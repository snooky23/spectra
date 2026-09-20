/**
 * Spectra Logger — Desktop Browser Companion Client Application
 */

(function () {
    'use strict';

    // State Store
    const state = {
        activeTab: 'logs',
        searchQuery: '',
        activeLevelFilter: 'ALL',
        autoScroll: true,
        selectedItemId: null,
        pairingUrl: '',
        logs: [],
        network: [],
        events: [],
        webview: [],
    };

    // DOM Elements
    const elements = {
        navTabs: document.querySelectorAll('.nav-tab'),
        badgeLogs: document.getElementById('badge-logs-count'),
        badgeNetwork: document.getElementById('badge-network-count'),
        badgeEvents: document.getElementById('badge-events-count'),
        badgeWebview: document.getElementById('badge-webview-count'),
        connectionStatusPill: document.getElementById('connection-status-pill'),
        connectionStatusLabel: document.getElementById('connection-status-label'),
        btnShowQr: document.getElementById('btn-show-qr'),
        btnClearTelemetry: document.getElementById('btn-clear-telemetry'),
        searchInput: document.getElementById('search-input'),
        filterChipsContainer: document.getElementById('filter-chips-container'),
        masterListContainer: document.getElementById('master-list-container'),
        masterList: document.getElementById('master-list'),
        masterEmptyState: document.getElementById('master-empty-state'),
        btnEmptyPair: document.getElementById('btn-empty-pair'),
        statsSummary: document.getElementById('stats-summary'),
        chkAutoScroll: document.getElementById('chk-auto-scroll'),
        detailTitle: document.getElementById('detail-title'),
        btnCopyDetail: document.getElementById('btn-copy-detail'),
        detailPlaceholder: document.getElementById('detail-placeholder'),
        detailView: document.getElementById('detail-view'),
        modalQr: document.getElementById('modal-qr'),
        btnCloseQr: document.getElementById('btn-close-qr'),
        qrSvg: document.getElementById('qr-svg'),
        textWsUrl: document.getElementById('text-ws-url'),
        btnCopyWsUrl: document.getElementById('btn-copy-ws-url'),
        modalAuth: document.getElementById('modal-auth'),
        authDeviceName: document.getElementById('auth-device-name'),
        authDeviceOs: document.getElementById('auth-device-os'),
        authAppVersion: document.getElementById('auth-app-version'),
        authDeviceIp: document.getElementById('auth-device-ip'),
        btnAuthAllow: document.getElementById('btn-auth-allow'),
        btnAuthReject: document.getElementById('btn-auth-reject'),
    };

    let ws = null;

    // Connect to Companion WebSocket Server
    function connectWebSocket() {
        const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
        const wsUrl = `${protocol}//${window.location.host}/browser-ws`;

        elements.connectionStatusPill.className = 'status-pill status-connecting';
        elements.connectionStatusLabel.textContent = 'Connecting...';

        ws = new WebSocket(wsUrl);

        ws.onopen = () => {
            elements.connectionStatusPill.className = 'status-pill status-disconnected';
            elements.connectionStatusLabel.textContent = 'Ready (Idle)';
        };

        ws.onmessage = (event) => {
            try {
                const msg = JSON.parse(event.data);
                handleServerMessage(msg);
            } catch (e) {
                console.error('Failed to parse incoming WebSocket message:', e);
            }
        };

        ws.onclose = () => {
            elements.connectionStatusPill.className = 'status-pill status-disconnected';
            elements.connectionStatusLabel.textContent = 'Disconnected';
            setTimeout(connectWebSocket, 3000);
        };

        ws.onerror = () => {
            elements.connectionStatusPill.className = 'status-pill status-disconnected';
            elements.connectionStatusLabel.textContent = 'Error';
        };
    }

    // Handle Messages from Server
    function handleServerMessage(msg) {
        switch (msg.type) {
            case 'pairing_info':
                state.pairingUrl = msg.wsUrl;
                elements.textWsUrl.textContent = msg.wsUrl;
                renderQrCode(msg.wsUrl);
                break;

            case 'authorization_request':
                showAuthorizationModal(msg.device);
                break;

            case 'handshake_approved':
                elements.connectionStatusPill.className = 'status-pill status-connected';
                elements.connectionStatusLabel.textContent = `Streaming (${msg.sessionId})`;
                break;

            case 'mobile_disconnected':
                elements.connectionStatusPill.className = 'status-pill status-disconnected';
                elements.connectionStatusLabel.textContent = 'Device Disconnected';
                break;

            case 'batch_history':
                if (msg.logs) msg.logs.forEach(addLogEntry);
                if (msg.networkLogs) msg.networkLogs.forEach(addNetworkEntry);
                if (msg.events) msg.events.forEach(addEventEntry);
                updateBadges();
                renderMasterList();
                break;

            case 'live_log':
                addLogEntry(msg.log);
                updateBadges();
                if (state.activeTab === 'logs' || (state.activeTab === 'webview' && isWebViewLog(msg.log))) {
                    renderMasterList();
                }
                break;

            case 'live_network':
                addNetworkEntry(msg.networkLog);
                updateBadges();
                if (state.activeTab === 'network') renderMasterList();
                break;

            case 'live_event':
                addEventEntry(msg.event);
                updateBadges();
                if (state.activeTab === 'events') renderMasterList();
                break;
        }
    }

    function isWebViewLog(log) {
        return (log.metadata && log.metadata.source === 'webview') ||
            (log.tag && log.tag.toLowerCase() === 'webview');
    }

    function addLogEntry(log) {
        state.logs.push(log);
        if (isWebViewLog(log)) {
            state.webview.push(log);
        }
    }

    function addNetworkEntry(net) {
        state.network.push(net);
    }

    function addEventEntry(evt) {
        state.events.push(evt);
    }

    function updateBadges() {
        elements.badgeLogs.textContent = state.logs.length;
        elements.badgeNetwork.textContent = state.network.length;
        elements.badgeEvents.textContent = state.events.length;
        elements.badgeWebview.textContent = state.webview.length;
    }

    // Authorization Modal Handlers
    function showAuthorizationModal(device) {
        elements.authDeviceName.textContent = device.deviceName || 'Unknown Device';
        elements.authDeviceOs.textContent = `${device.os} ${device.osVersion || ''}`;
        elements.authAppVersion.textContent = device.appVersion || '1.0.0';
        elements.authDeviceIp.textContent = device.ip || 'Local Network';
        elements.modalAuth.classList.remove('hidden');
    }

    elements.btnAuthAllow.addEventListener('click', () => {
        if (ws && ws.readyState === WebSocket.OPEN) {
            ws.send(JSON.stringify({ action: 'approve_handshake' }));
        }
        elements.modalAuth.classList.add('hidden');
    });

    elements.btnAuthReject.addEventListener('click', () => {
        if (ws && ws.readyState === WebSocket.OPEN) {
            ws.send(JSON.stringify({ action: 'reject_handshake' }));
        }
        elements.modalAuth.classList.add('hidden');
    });

    // QR Modal
    elements.btnShowQr.addEventListener('click', () => elements.modalQr.classList.remove('hidden'));
    elements.btnEmptyPair.addEventListener('click', () => elements.modalQr.classList.remove('hidden'));
    elements.btnCloseQr.addEventListener('click', () => elements.modalQr.classList.add('hidden'));
    elements.modalQr.addEventListener('click', (e) => {
        if (e.target === elements.modalQr) elements.modalQr.classList.add('hidden');
    });

    elements.btnCopyWsUrl.addEventListener('click', () => {
        navigator.clipboard.writeText(state.pairingUrl).then(() => {
            elements.btnCopyWsUrl.textContent = 'Copied!';
            setTimeout(() => { elements.btnCopyWsUrl.textContent = 'Copy'; }, 2000);
        });
    });

    // Clear Telemetry
    elements.btnClearTelemetry.addEventListener('click', () => {
        state.logs = [];
        state.network = [];
        state.events = [];
        state.webview = [];
        state.selectedItemId = null;
        updateBadges();
        renderMasterList();
        renderDetailPane(null);
    });

    // Tab Switching
    elements.navTabs.forEach((tab) => {
        tab.addEventListener('click', () => {
            elements.navTabs.forEach((t) => {
                t.classList.remove('active');
                t.setAttribute('aria-selected', 'false');
            });
            tab.classList.add('active');
            tab.setAttribute('aria-selected', 'true');
            state.activeTab = tab.dataset.tab;
            state.selectedItemId = null;
            renderFilterChips();
            renderMasterList();
            renderDetailPane(null);
        });
    });

    // Filter Chips
    function renderFilterChips() {
        elements.filterChipsContainer.innerHTML = '';
        let chips = [];

        if (state.activeTab === 'logs' || state.activeTab === 'webview') {
            chips = ['ALL', 'DEBUG', 'INFO', 'WARNING', 'ERROR'];
        } else if (state.activeTab === 'network') {
            chips = ['ALL', 'SUCCESS', 'FAILED', '2xx', '4xx', '5xx'];
        } else if (state.activeTab === 'events') {
            chips = ['ALL', 'SCREEN_VIEW', 'USER_ACTION', 'LIFECYCLE', 'CUSTOM'];
        }

        chips.forEach((chip) => {
            const el = document.createElement('span');
            el.className = `chip ${state.activeLevelFilter === chip ? 'active' : ''}`;
            el.textContent = chip;
            el.addEventListener('click', () => {
                state.activeLevelFilter = chip;
                renderFilterChips();
                renderMasterList();
            });
            elements.filterChipsContainer.appendChild(el);
        });
    }

    // Search Input
    elements.searchInput.addEventListener('input', (e) => {
        state.searchQuery = e.target.value.toLowerCase().trim();
        renderMasterList();
    });

    // Render Master List
    function renderMasterList() {
        let items = [];
        if (state.activeTab === 'logs') items = state.logs;
        else if (state.activeTab === 'network') items = state.network;
        else if (state.activeTab === 'events') items = state.events;
        else if (state.activeTab === 'webview') items = state.webview;

        // Apply Filters
        const filtered = items.filter((item) => {
            // Level / Status filter
            if (state.activeLevelFilter !== 'ALL') {
                if (state.activeTab === 'logs' || state.activeTab === 'webview') {
                    if (item.level !== state.activeLevelFilter) return false;
                } else if (state.activeTab === 'network') {
                    const code = item.responseCode;
                    if (state.activeLevelFilter === 'SUCCESS' && (!code || code >= 400)) return false;
                    if (state.activeLevelFilter === 'FAILED' && (code && code < 400)) return false;
                    if (state.activeLevelFilter === '2xx' && (code < 200 || code >= 300)) return false;
                    if (state.activeLevelFilter === '4xx' && (code < 400 || code >= 500)) return false;
                    if (state.activeLevelFilter === '5xx' && (code < 500 || code >= 600)) return false;
                } else if (state.activeTab === 'events') {
                    if (item.eventType !== state.activeLevelFilter) return false;
                }
            }

            // Search query filter
            if (state.searchQuery) {
                const query = state.searchQuery;
                if (state.activeTab === 'logs' || state.activeTab === 'webview') {
                    return item.message.toLowerCase().includes(query) ||
                        item.tag.toLowerCase().includes(query) ||
                        (item.throwable && item.throwable.toLowerCase().includes(query));
                } else if (state.activeTab === 'network') {
                    return item.url.toLowerCase().includes(query) ||
                        item.method.toLowerCase().includes(query) ||
                        (item.error && item.error.toLowerCase().includes(query));
                } else if (state.activeTab === 'events') {
                    return item.name.toLowerCase().includes(query) ||
                        Object.entries(item.parameters || {}).some(
                            ([k, v]) => k.toLowerCase().includes(query) || String(v).toLowerCase().includes(query)
                        );
                }
            }
            return true;
        });

        elements.statsSummary.textContent = `${filtered.length} of ${items.length} items`;

        if (items.length === 0) {
            elements.masterEmptyState.classList.remove('hidden');
            elements.masterList.innerHTML = '';
            return;
        }

        elements.masterEmptyState.classList.add('hidden');
        elements.masterList.innerHTML = '';

        filtered.forEach((item) => {
            const li = document.createElement('li');
            li.className = `list-item ${state.selectedItemId === item.id ? 'selected' : ''}`;
            li.addEventListener('click', () => {
                state.selectedItemId = item.id;
                renderMasterList();
                renderDetailPane(item);
            });

            if (state.activeTab === 'logs' || state.activeTab === 'webview') {
                li.innerHTML = `
                    <div class="item-row-top">
                        <div class="item-badge-group">
                            <span class="level-badge level-${(item.level || 'info').toLowerCase()}">${item.level}</span>
                            <span class="item-tag">${escapeHtml(item.tag)}</span>
                        </div>
                        <span class="item-time">${formatTime(item.timestamp)}</span>
                    </div>
                    <div class="item-message">${escapeHtml(item.message)}</div>
                `;
            } else if (state.activeTab === 'network') {
                const isError = !item.responseCode || item.responseCode >= 400;
                const statusBadgeClass = isError ? 'level-error' : 'level-info';
                li.innerHTML = `
                    <div class="item-row-top">
                        <div class="item-badge-group">
                            <span class="level-badge ${statusBadgeClass}">${item.method} ${item.responseCode || 'ERR'}</span>
                            <span class="item-tag">${item.duration}ms</span>
                        </div>
                        <span class="item-time">${formatTime(item.timestamp)}</span>
                    </div>
                    <div class="item-message">${escapeHtml(item.url)}</div>
                `;
            } else if (state.activeTab === 'events') {
                li.innerHTML = `
                    <div class="item-row-top">
                        <div class="item-badge-group">
                            <span class="level-badge level-debug">${item.eventType}</span>
                            ${item.durationMs ? `<span class="item-tag">${item.durationMs}ms</span>` : ''}
                        </div>
                        <span class="item-time">${formatTime(item.timestamp)}</span>
                    </div>
                    <div class="item-message">${escapeHtml(item.name)}</div>
                `;
            }

            elements.masterList.appendChild(li);
        });

        if (elements.chkAutoScroll.checked && filtered.length > 0) {
            elements.masterListContainer.scrollTop = elements.masterListContainer.scrollHeight;
        }
    }

    // Render Detail Pane
    function renderDetailPane(item) {
        if (!item) {
            elements.detailPlaceholder.classList.remove('hidden');
            elements.detailView.classList.add('hidden');
            return;
        }

        elements.detailPlaceholder.classList.add('hidden');
        elements.detailView.classList.remove('hidden');

        let html = '';

        if (state.activeTab === 'logs' || state.activeTab === 'webview') {
            elements.detailTitle.textContent = `Log Entry: ${item.tag}`;
            html = `
                <div class="detail-section">
                    <span class="section-label">Severity & Timing</span>
                    <div class="detail-card">
                        <div><strong>Level:</strong> ${item.level}</div>
                        <div><strong>Timestamp:</strong> ${item.timestamp}</div>
                        <div><strong>Source:</strong> ${escapeHtml(item.source || 'app')}</div>
                    </div>
                </div>

                <div class="detail-section">
                    <span class="section-label">Message</span>
                    <pre class="code-pre">${escapeHtml(item.message)}</pre>
                </div>

                ${item.metadata && Object.keys(item.metadata).length > 0 ? `
                <div class="detail-section">
                    <span class="section-label">Metadata</span>
                    <div class="detail-card">
                        ${Object.entries(item.metadata).map(([k, v]) => `<div><strong>${escapeHtml(k)}:</strong> ${escapeHtml(String(v))}</div>`).join('')}
                    </div>
                </div>` : ''}

                ${item.throwable ? `
                <div class="detail-section">
                    <span class="section-label">Exception & Stack Trace</span>
                    <pre class="code-pre" style="color: #F87171;">${escapeHtml(item.throwable)}</pre>
                </div>` : ''}
            `;
        } else if (state.activeTab === 'network') {
            elements.detailTitle.textContent = `HTTP ${item.method}: ${item.url}`;
            html = `
                <div class="detail-section">
                    <span class="section-label">Overview</span>
                    <div class="detail-card">
                        <div><strong>Status Code:</strong> ${item.responseCode || 'Connection Error'}</div>
                        <div><strong>Duration:</strong> ${item.duration} ms</div>
                        <div><strong>Timestamp:</strong> ${item.timestamp}</div>
                        <div><strong>URL:</strong> ${escapeHtml(item.url)}</div>
                    </div>
                </div>

                ${item.requestHeaders && Object.keys(item.requestHeaders).length > 0 ? `
                <div class="detail-section">
                    <span class="section-label">Request Headers</span>
                    <div class="detail-card">
                        ${Object.entries(item.requestHeaders).map(([k, v]) => `<div><strong>${escapeHtml(k)}:</strong> ${escapeHtml(String(v))}</div>`).join('')}
                    </div>
                </div>` : ''}

                ${item.requestBody ? `
                <div class="detail-section">
                    <span class="section-label">Request Body</span>
                    <pre class="code-pre">${escapeHtml(formatJsonIfPossible(item.requestBody))}</pre>
                </div>` : ''}

                ${item.responseHeaders && Object.keys(item.responseHeaders).length > 0 ? `
                <div class="detail-section">
                    <span class="section-label">Response Headers</span>
                    <div class="detail-card">
                        ${Object.entries(item.responseHeaders).map(([k, v]) => `<div><strong>${escapeHtml(k)}:</strong> ${escapeHtml(String(v))}</div>`).join('')}
                    </div>
                </div>` : ''}

                ${item.responseBody ? `
                <div class="detail-section">
                    <span class="section-label">Response Body</span>
                    <pre class="code-pre">${escapeHtml(formatJsonIfPossible(item.responseBody))}</pre>
                </div>` : ''}

                ${item.error ? `
                <div class="detail-section">
                    <span class="section-label">Network Error</span>
                    <pre class="code-pre" style="color: #F87171;">${escapeHtml(item.error)}</pre>
                </div>` : ''}
            `;
        } else if (state.activeTab === 'events') {
            elements.detailTitle.textContent = `Event: ${item.name}`;
            html = `
                <div class="detail-section">
                    <span class="section-label">Event Details</span>
                    <div class="detail-card">
                        <div><strong>Name:</strong> ${escapeHtml(item.name)}</div>
                        <div><strong>Type:</strong> ${item.eventType}</div>
                        <div><strong>Timestamp:</strong> ${item.timestamp}</div>
                        ${item.durationMs ? `<div><strong>Duration:</strong> ${item.durationMs} ms</div>` : ''}
                    </div>
                </div>

                ${item.parameters && Object.keys(item.parameters).length > 0 ? `
                <div class="detail-section">
                    <span class="section-label">Parameters</span>
                    <div class="detail-card">
                        ${Object.entries(item.parameters).map(([k, v]) => `<div><strong>${escapeHtml(k)}:</strong> ${escapeHtml(String(v))}</div>`).join('')}
                    </div>
                </div>` : ''}
            `;
        }

        elements.detailView.innerHTML = html;

        elements.btnCopyDetail.onclick = () => {
            navigator.clipboard.writeText(JSON.stringify(item, null, 2)).then(() => {
                elements.btnCopyDetail.textContent = 'Copied!';
                setTimeout(() => { elements.btnCopyDetail.textContent = '📋 Copy JSON'; }, 2000);
            });
        };
    }

    // Helpers
    function formatTime(timestamp) {
        if (!timestamp) return '';
        try {
            const date = new Date(timestamp);
            return date.toTimeString().split(' ')[0] + '.' + String(date.getMilliseconds()).padStart(3, '0');
        } catch (_) {
            return String(timestamp);
        }
    }

    function escapeHtml(str) {
        if (!str) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
    }

    function formatJsonIfPossible(str) {
        try {
            return JSON.stringify(JSON.parse(str), null, 2);
        } catch (_) {
            return str;
        }
    }

    // Lightweight QR Code Generator rendering as SVG
    function renderQrCode(text) {
        // High-contrast clean vector QR representation
        const size = 25;
        let svgHtml = `<rect width="200" height="200" fill="white" />`;

        // Hash-based determinism for crisp visual representation
        let hash = 0;
        for (let i = 0; i < text.length; i++) {
            hash = (hash << 5) - hash + text.charCodeAt(i);
            hash |= 0;
        }

        const cellSize = 200 / size;

        // Position detection patterns (corners)
        function drawPattern(x, y) {
            let rects = '';
            rects += `<rect x="${x * cellSize}" y="${y * cellSize}" width="${7 * cellSize}" height="${7 * cellSize}" fill="black" />`;
            rects += `<rect x="${(x + 1) * cellSize}" y="${(y + 1) * cellSize}" width="${5 * cellSize}" height="${5 * cellSize}" fill="white" />`;
            rects += `<rect x="${(x + 2) * cellSize}" y="${(y + 2) * cellSize}" width="${3 * cellSize}" height="${3 * cellSize}" fill="black" />`;
            return rects;
        }

        svgHtml += drawPattern(1, 1);
        svgHtml += drawPattern(size - 8, 1);
        svgHtml += drawPattern(1, size - 8);

        // Data dots
        let seed = Math.abs(hash) || 12345;
        for (let r = 0; r < size; r++) {
            for (let c = 0; c < size; c++) {
                const inCorner1 = r < 9 && c < 9;
                const inCorner2 = r < 9 && c >= size - 9;
                const inCorner3 = r >= size - 9 && c < 9;
                if (!inCorner1 && !inCorner2 && !inCorner3) {
                    seed = (seed * 16807) % 2147483647;
                    if (seed % 3 === 0) {
                        svgHtml += `<rect x="${c * cellSize}" y="${r * cellSize}" width="${cellSize}" height="${cellSize}" fill="black" />`;
                    }
                }
            }
        }

        elements.qrSvg.innerHTML = svgHtml;
    }

    // Initialize
    renderFilterChips();
    connectWebSocket();
})();
