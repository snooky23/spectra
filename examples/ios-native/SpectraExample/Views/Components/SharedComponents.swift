import SwiftUI

// MARK: - Reusable Components

/// Reusable log-generating button component
public struct LogButton: View {
    public let label: String
    public let icon: String
    public let backgroundColor: Color
    public let action: () -> Void

    public init(label: String, icon: String, backgroundColor: Color, action: @escaping () -> Void) {
        self.label = label
        self.icon = icon
        self.backgroundColor = backgroundColor
        self.action = action
    }

    public var body: some View {
        Button(action: action) {
            Label(label, systemImage: icon)
                .frame(maxWidth: .infinity)
                .padding()
                .background(backgroundColor.opacity(0.1))
                .cornerRadius(10)
        }
        .foregroundColor(.primary)
    }
}

/// Reusable branding card component
public struct BrandingCard: View {
    public let icon: String
    public let title: String
    public let subtitle: String

    public init(icon: String, title: String, subtitle: String) {
        self.icon = icon
        self.title = title
        self.subtitle = subtitle
    }

    public var body: some View {
        VStack(spacing: 10) {
            Image(systemName: icon)
                .font(.system(size: 80))
                .foregroundColor(.blue)

            Text(title)
                .font(.largeTitle)
                .fontWeight(.bold)

            Text(subtitle)
                .font(.subheadline)
                .foregroundColor(.secondary)
        }
    }
}

/// Section header for grouping UI elements
public struct SectionHeader: View {
    public let title: String

    public init(title: String) {
        self.title = title
    }

    public var body: some View {
        Text(title)
            .font(.headline)
    }
}

// MARK: - Previews

#Preview("LogButton - Info") {
    LogButton(
        label: "Tap Me (Generates Log)",
        icon: "hand.tap",
        backgroundColor: .blue,
        action: {}
    )
    .padding()
}

#Preview("BrandingCard") {
    BrandingCard(
        icon: "app.badge.checkmark",
        title: "Example App",
        subtitle: "with Spectra Logger Integration"
    )
    .padding()
}

#Preview("SectionHeader") {
    SectionHeader(title: "Example Actions")
        .padding()
}
