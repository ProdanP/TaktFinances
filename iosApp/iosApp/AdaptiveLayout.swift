import CoreGraphics

/// Layout breakpoints in points, measured on the space a screen's content actually gets (the
/// split view's detail column on iPad), not on the device. Same values as the Compose screens;
/// see docs/system/adaptive-ui.md.
enum AdaptiveLayout {
    /// Below this width the content is phone-sized.
    static let mediumWidth: CGFloat = 600
    /// From this width two panes fit side by side.
    static let twoPaneWidth: CGFloat = 840
    /// Below this height a pane cannot stay pinned without hiding the content beside it.
    static let shortHeight: CGFloat = 480
    /// Readable width of a single phone-like column on larger screens.
    static let phoneColumnWidth: CGFloat = 480
    /// Readable width of a form column.
    static let formColumnWidth: CGFloat = 560
}
