#!/usr/bin/env node

/**
 * Spectra Logger — Desktop Browser Companion Server
 *
 * Provides a zero-dependency HTTP server and WebSocket bridge enabling real-time
 * telemetry streaming from mobile devices over local Wi-Fi.
 */

const http = require('http');
const fs = require('fs');
const path = require('path');
const os = require('os');
const crypto = require('crypto');

const PORT = process.env.PORT || 9292;
const PUBLIC_DIR = path.join(__dirname, 'public');

// Find primary local LAN IPv4 address
function getLocalIpAddress() {
    const interfaces = os.networkInterfaces();
    for (const name of Object.keys(interfaces)) {
        for (const iface of interfaces[name]) {
            if (iface.family === 'IPv4' && !iface.internal) {
                return iface.address;
            }
        }
    }
    return '127.0.0.1';
}

const localIp = getLocalIpAddress();
let pairingToken = crypto.randomBytes(8).toString('hex');
let activeMobileSocket = null;
let activeBrowserSocket = null;
let activeSessionId = null;

// HTTP Request Handler
const server = http.createServer((req, res) => {
    let reqPath = req.url.split('?')[0];
    if (reqPath === '/') reqPath = '/index.html';

    // Status API
    if (reqPath === '/api/info') {
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({
            ip: localIp,
            port: PORT,
            token: pairingToken,
            wsUrl: `ws://${localIp}:${PORT}/spectra-ws?token=${pairingToken}`
        }));
        return;
    }

    const filePath = path.join(PUBLIC_DIR, reqPath);
    const ext = path.extname(filePath);

    const mimeTypes = {
        '.html': 'text/html',
        '.js': 'application/javascript',
        '.css': 'text/css',
        '.json': 'application/json',
        '.png': 'image/png',
        '.svg': 'image/svg+xml'
    };

    const contentType = mimeTypes[ext] || 'text/plain';

    fs.readFile(filePath, (err, content) => {
        if (err) {
            if (err.code === 'ENOENT') {
                res.writeHead(404, { 'Content-Type': 'text/plain' });
                res.end('404 Not Found');
            } else {
                res.writeHead(500);
                res.end(`Server Error: ${err.code}`);
            }
        } else {
            res.writeHead(200, { 'Content-Type': contentType });
            res.end(content, 'utf-8');
        }
    });
});

// Minimal Pure Node.js WebSocket Server (RFC 6455)
server.on('upgrade', (req, socket, head) => {
    const urlParts = req.url.split('?');
    const pathname = urlParts[0];

    if (pathname !== '/spectra-ws' && pathname !== '/browser-ws') {
        socket.destroy();
        return;
    }

    const key = req.headers['sec-websocket-key'];
    if (!key) {
        socket.destroy();
        return;
    }

    const acceptKey = crypto
        .createHash('sha1')
        .update(key + '258EAFA5-E914-47DA-95CA-C5AB0DC85B11')
        .digest('base64');

    const headers = [
        'HTTP/1.1 101 Switching Protocols',
        'Upgrade: websocket',
        'Connection: Upgrade',
        `Sec-WebSocket-Accept: ${acceptKey}`
    ];

    socket.write(headers.join('\r\n') + '\r\n\r\n');

    // Attach frame parser
    setupWebSocket(socket, pathname === '/browser-ws');
});

function setupWebSocket(socket, isBrowser) {
    let buffer = Buffer.alloc(0);

    if (isBrowser) {
        activeBrowserSocket = socket;
        console.log('🖥️ Desktop browser dashboard connected.');

        // Send initial pairing info
        sendWsMessage(socket, {
            type: 'pairing_info',
            ip: localIp,
            port: PORT,
            token: pairingToken,
            wsUrl: `ws://${localIp}:${PORT}/spectra-ws?token=${pairingToken}`
        });
    } else {
        activeMobileSocket = socket;
        console.log('📱 Mobile device connected to WebSocket endpoint.');
    }

    socket.on('data', (chunk) => {
        buffer = Buffer.concat([buffer, chunk]);
        while (buffer.length >= 2) {
            const firstByte = buffer[0];
            const secondByte = buffer[1];
            const opcode = firstByte & 0x0f;
            const isMasked = (secondByte & 0x80) !== 0;
            let payloadLength = secondByte & 0x7f;
            let offset = 2;

            if (payloadLength === 126) {
                if (buffer.length < 4) return;
                payloadLength = buffer.readUInt16BE(2);
                offset = 4;
            } else if (payloadLength === 127) {
                if (buffer.length < 10) return;
                payloadLength = Number(buffer.readBigUInt64BE(2));
                offset = 10;
            }

            let maskKey = null;
            if (isMasked) {
                if (buffer.length < offset + 4) return;
                maskKey = buffer.slice(offset, offset + 4);
                offset += 4;
            }

            if (buffer.length < offset + payloadLength) return;

            const payload = buffer.slice(offset, offset + payloadLength);
            buffer = buffer.slice(offset + payloadLength);

            if (isMasked && maskKey) {
                for (let i = 0; i < payload.length; i++) {
                    payload[i] ^= maskKey[i % 4];
                }
            }

            if (opcode === 0x08) {
                // Close frame
                socket.end();
                return;
            } else if (opcode === 0x09) {
                // Ping -> Send Pong
                sendWsFrame(socket, 0x0A, payload);
            } else if (opcode === 0x01) {
                // Text frame
                const text = payload.toString('utf8');
                try {
                    const message = JSON.parse(text);
                    handleWsMessage(socket, isBrowser, message);
                } catch (e) {
                    console.error('Failed to parse WebSocket JSON:', e);
                }
            }
        }
    });

    socket.on('close', () => {
        if (isBrowser) {
            if (activeBrowserSocket === socket) activeBrowserSocket = null;
            console.log('🖥️ Desktop browser dashboard disconnected.');
        } else {
            if (activeMobileSocket === socket) {
                activeMobileSocket = null;
                activeSessionId = null;
                if (activeBrowserSocket) {
                    sendWsMessage(activeBrowserSocket, { type: 'mobile_disconnected' });
                }
                console.log('📱 Mobile device disconnected.');
            }
        }
    });

    socket.on('error', (err) => {
        console.error('Socket error:', err.message);
    });
}

function sendWsFrame(socket, opcode, payload) {
    if (!socket || socket.destroyed) return;
    const length = payload.length;
    let header;

    if (length < 126) {
        header = Buffer.alloc(2);
        header[0] = 0x80 | opcode;
        header[1] = length;
    } else if (length < 65536) {
        header = Buffer.alloc(4);
        header[0] = 0x80 | opcode;
        header[1] = 126;
        header.writeUInt16BE(length, 2);
    } else {
        header = Buffer.alloc(10);
        header[0] = 0x80 | opcode;
        header[1] = 127;
        header.writeBigUInt64BE(BigInt(length), 2);
    }

    try {
        socket.write(Buffer.concat([header, payload]));
    } catch (_) {}
}

function sendWsMessage(socket, obj) {
    const json = JSON.stringify(obj);
    sendWsFrame(socket, 0x01, Buffer.from(json, 'utf8'));
}

function handleWsMessage(socket, isBrowser, msg) {
    if (isBrowser) {
        // Handle commands from browser
        if (msg.action === 'approve_handshake') {
            if (activeMobileSocket) {
                activeSessionId = crypto.randomBytes(6).toString('hex');
                sendWsMessage(activeMobileSocket, {
                    type: 'handshake_response',
                    accepted: true,
                    sessionId: activeSessionId
                });
                sendWsMessage(socket, {
                    type: 'handshake_approved',
                    sessionId: activeSessionId
                });
                console.log(`✅ Handshake APPROVED by desktop. Session ID: ${activeSessionId}`);
            }
        } else if (msg.action === 'reject_handshake') {
            if (activeMobileSocket) {
                sendWsMessage(activeMobileSocket, {
                    type: 'handshake_response',
                    accepted: false,
                    sessionId: '',
                    reason: 'Rejected by desktop user'
                });
                activeMobileSocket.end();
                activeMobileSocket = null;
            }
            console.log('❌ Handshake REJECTED by desktop.');
        } else if (msg.action === 'refresh_token') {
            pairingToken = crypto.randomBytes(8).toString('hex');
            sendWsMessage(socket, {
                type: 'pairing_info',
                ip: localIp,
                port: PORT,
                token: pairingToken,
                wsUrl: `ws://${localIp}:${PORT}/spectra-ws?token=${pairingToken}`
            });
        }
    } else {
        // Mobile messages
        const msgType = msg.type;

        if (msgType === 'handshake_request') {
            console.log(`🔔 Incoming Handshake Request from ${msg.deviceName} (${msg.os} ${msg.osVersion})`);

            // Verify token matches if provided
            const tokenValid = !pairingToken || msg.token === pairingToken;
            if (!tokenValid) {
                console.warn('⚠️ Rejected handshake due to invalid pairing token.');
                sendWsMessage(socket, {
                    type: 'handshake_response',
                    accepted: false,
                    sessionId: '',
                    reason: 'Invalid pairing token'
                });
                socket.end();
                return;
            }

            // Forward approval request to browser dashboard
            if (activeBrowserSocket) {
                sendWsMessage(activeBrowserSocket, {
                    type: 'authorization_request',
                    device: {
                        deviceId: msg.deviceId,
                        deviceName: msg.deviceName,
                        os: msg.os,
                        osVersion: msg.osVersion,
                        appVersion: msg.appVersion,
                        ip: socket.remoteAddress
                    }
                });
            } else {
                console.warn('⚠️ No active desktop browser open to authorize device.');
            }
        } else if (msgType === 'batch_history' || msgType === 'live_log' || msgType === 'live_network' || msgType === 'live_event') {
            // Forward telemetry to browser dashboard
            if (activeBrowserSocket) {
                sendWsMessage(activeBrowserSocket, msg);
            }
        } else if (msgType === 'pong') {
            // Mobile heartbeat response
        }
    }
}

server.listen(PORT, '0.0.0.0', () => {
    console.log('\n======================================================');
    console.log('🚀 Spectra Logger Desktop Browser Companion is ACTIVE');
    console.log('======================================================');
    console.log(`🌐 Dashboard URL:    http://localhost:${PORT}`);
    console.log(`📡 Local Wi-Fi URL:  http://${localIp}:${PORT}`);
    console.log(`📱 Mobile WS URL:    ws://${localIp}:${PORT}/spectra-ws?token=${pairingToken}`);
    console.log('======================================================\n');
});
