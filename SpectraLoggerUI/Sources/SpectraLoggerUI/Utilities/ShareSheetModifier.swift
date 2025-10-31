import SwiftUI

// MARK: - Share Sheet Modifier

extension View {
    func shareSheet(items: Binding<[Any]>) -> some View {
        modifier(ShareSheetModifier(items: items))
    }
}

struct ShareSheetModifier: ViewModifier {
    @Binding var items: [Any]

    func body(content: Content) -> some View {
        content.background(
            ShareSheetPresenter(items: $items)
        )
    }
}

struct ShareSheetPresenter: UIViewControllerRepresentable {
    @Binding var items: [Any]

    func makeUIViewController(context: Context) -> UIViewController {
        return UIViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        guard !items.isEmpty else { return }

        let activityViewController = UIActivityViewController(
            activityItems: items,
            applicationActivities: nil
        )

        // Handle iPad support
        if let popover = activityViewController.popoverPresentationController {
            popover.sourceView = uiViewController.view
            popover.sourceRect = CGRect(x: uiViewController.view.bounds.midX, y: uiViewController.view.bounds.midY, width: 0, height: 0)
            popover.permittedArrowDirections = []
        }

        DispatchQueue.main.async {
            uiViewController.present(activityViewController, animated: true) {
                items = []
            }
        }
    }
}
