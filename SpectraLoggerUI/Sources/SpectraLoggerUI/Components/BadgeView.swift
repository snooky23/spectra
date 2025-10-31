import SwiftUI

/// A reusable badge component for displaying tags with custom styling
///
/// Used throughout the app for:
/// - Log levels (Info, Warning, Error, etc.)
/// - HTTP methods (GET, POST, PUT, etc.)
/// - HTTP status codes (200, 404, etc.)
///
/// Example usage:
/// ```swift
/// BadgeView(text: "Info", backgroundColor: .green, foregroundColor: .green)
/// BadgeView(text: "GET", backgroundColor: .blue, foregroundColor: .blue)
/// ```
struct BadgeView: View {
    let text: String
    let backgroundColor: Color
    let foregroundColor: Color

    var body: some View {
        Text(text)
            .font(.caption)
            .fontWeight(.semibold)
            .padding(.horizontal, 8)
            .padding(.vertical, 2)
            .background(backgroundColor.opacity(0.2))
            .foregroundColor(foregroundColor)
            .cornerRadius(4)
    }
}

// MARK: - Previews

#Preview("BadgeView - Info") {
    HStack(spacing: 12) {
        BadgeView(text: "Info", backgroundColor: .green, foregroundColor: .green)
        BadgeView(text: "Warning", backgroundColor: .orange, foregroundColor: .orange)
        BadgeView(text: "Error", backgroundColor: .red, foregroundColor: .red)
    }
    .padding()
}
