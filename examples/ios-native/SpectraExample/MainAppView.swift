import SwiftUI

/// Main app screen with example content and button to open Spectra Logger
struct MainAppView: View {
    @State private var showSpectraLogger = false
    @State private var counter = 0

    var body: some View {
        NavigationView {
            VStack(spacing: 30) {
                // App branding
                VStack(spacing: 10) {
                    Image(systemName: "app.badge.checkmark")
                        .font(.system(size: 80))
                        .foregroundColor(.blue)

                    Text("Example App")
                        .font(.largeTitle)
                        .fontWeight(.bold)

                    Text("with Spectra Logger Integration")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }
                .padding(.top, 50)

                Spacer()

                // Example actions that generate logs
                VStack(spacing: 20) {
                    Text("Example Actions")
                        .font(.headline)

                    Button(action: {
                        counter += 1
                        print("Button tapped \(counter) times")
                    }) {
                        Label("Tap Me (Generates Log)", systemImage: "hand.tap")
                            .frame(maxWidth: .infinity)
                            .padding()
                            .background(Color.blue.opacity(0.1))
                            .cornerRadius(10)
                    }

                    Button(action: {
                        print("Warning log generated")
                    }) {
                        Label("Generate Warning", systemImage: "exclamationmark.triangle")
                            .frame(maxWidth: .infinity)
                            .padding()
                            .background(Color.orange.opacity(0.1))
                            .cornerRadius(10)
                    }

                    Button(action: {
                        print("Error log generated")
                    }) {
                        Label("Generate Error", systemImage: "xmark.circle")
                            .frame(maxWidth: .infinity)
                            .padding()
                            .background(Color.red.opacity(0.1))
                            .cornerRadius(10)
                    }
                }
                .padding(.horizontal)

                Spacer()

                // Open Spectra Logger button
                Button(action: {
                    showSpectraLogger = true
                    print("Opening Spectra Logger UI")
                }) {
                    HStack {
                        Image(systemName: "doc.text.magnifyingglass")
                        Text("Open Spectra Logger")
                            .fontWeight(.semibold)
                    }
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(Color.purple)
                    .foregroundColor(.white)
                    .cornerRadius(12)
                }
                .padding(.horizontal)
                .padding(.bottom, 30)
            }
            .navigationBarTitleDisplayMode(.inline)
            .sheet(isPresented: $showSpectraLogger) {
                ContentView()
            }
        }
    }
}

#Preview {
    MainAppView()
}
