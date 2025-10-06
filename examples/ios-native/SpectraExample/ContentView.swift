import SwiftUI

/// SpectraLoggerScreen - Placeholder for the complete SDK logger UI
///
/// In the actual SDK, this would be the complete Spectra Logger screen with:
/// - Logs tab with filtering and search
/// - Network tab for network logs
/// - Settings tab with export functionality
/// - Bottom navigation with live log counts
///
/// For now, this is a placeholder showing what users will get from the SDK.
struct SpectraLoggerScreen: View {
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationView {
            VStack(spacing: 30) {
                Image(systemName: "doc.text.magnifyingglass")
                    .font(.system(size: 80))
                    .foregroundColor(.purple)

                Text("Spectra Logger SDK")
                    .font(.largeTitle)
                    .fontWeight(.bold)

                Text("Complete logger UI with all tabs")
                    .font(.subheadline)
                    .foregroundColor(.secondary)

                VStack(alignment: .leading, spacing: 12) {
                    FeatureRow(icon: "list.bullet.rectangle", text: "Logs Tab (with filtering & search)")
                    FeatureRow(icon: "network", text: "Network Tab (request/response logs)")
                    FeatureRow(icon: "gearshape", text: "Settings Tab (export & management)")
                    FeatureRow(icon: "chart.bar", text: "Live log counts on navigation")
                }
                .padding()
                .background(Color.purple.opacity(0.1))
                .cornerRadius(12)
                .padding(.horizontal)

                Spacer()
            }
            .navigationTitle("Spectra Logger")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Done") {
                        dismiss()
                    }
                }
            }
        }
    }
}

struct FeatureRow: View {
    let icon: String
    let text: String

    var body: some View {
        HStack {
            Image(systemName: icon)
                .foregroundColor(.purple)
                .frame(width: 24)
            Text(text)
                .font(.body)
        }
    }
}

#Preview {
    SpectraLoggerScreen()
}
