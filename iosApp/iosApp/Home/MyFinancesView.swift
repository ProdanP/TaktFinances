import SharedLogic
import SwiftUI

/// The My Finances tab body; picks its layout from the width it gets (the split view's detail
/// column on iPad).
struct MyFinancesView: View {
    let content: MyFinancesContent
    let balances: [Int64: Int64]
    let palette: FinancesColorScheme
    let send: (HomeIntent) -> Void
    let onAddFirstAccount: () -> Void
    /// Opens Account Detail; `checkIn` also opens its check-in sheet for the current month.
    let onOpenAccount: (_ id: Int64, _ checkIn: Bool) -> Void

    @Environment(\.horizontalSizeClass) private var horizontalSizeClass

    var body: some View {
        GeometryReader { geometry in
            let twoPanes = geometry.size.width >= AdaptiveLayout.twoPaneWidth
            Group {
                switch content {
                case .loading:
                    ProgressView().frame(maxWidth: .infinity, maxHeight: .infinity)
                case .failed:
                    centered { loadFailedCard }
                case .empty:
                    centered { addFirstAccountCard }
                case let .accounts(accounts, selectedId):
                    let index = accounts.firstIndex { $0.id == selectedId } ?? 0
                    // Arrows for pointer and keyboard users: on iPad, or whenever two panes fit.
                    let showArrows = twoPanes || horizontalSizeClass == .regular
                    if twoPanes {
                        HStack(alignment: .top, spacing: 24) {
                            swiperWithDots(accounts, index: index, selectedId: selectedId, showArrows: showArrows)
                                .frame(width: 420)
                            recentActivity(accounts[index])
                                .frame(maxHeight: .infinity, alignment: .top)
                        }
                        .padding(.horizontal, 16)
                        .padding(.vertical, 8)
                    } else {
                        ScrollView {
                            VStack(spacing: 16) {
                                swiperWithDots(accounts, index: index, selectedId: selectedId, showArrows: showArrows)
                                recentActivity(accounts[index])
                                    .padding(.horizontal, 16)
                            }
                            .padding(.vertical, 8)
                            .frame(maxWidth: AdaptiveLayout.phoneColumnWidth)
                            .frame(maxWidth: .infinity)
                        }
                    }
                }
            }
        }
        .background(palette.background)
        .onAppear { send(.shown) }
    }

    // MARK: Swiper

    private func swiperWithDots(_ accounts: [SavedAccount], index: Int, selectedId: Int64, showArrows: Bool) -> some View {
        VStack(spacing: 12) {
            HStack(spacing: 4) {
                if showArrows {
                    arrow("chevron.left", label: "Previous account", enabled: index > 0) { send(.previousTapped) }
                }
                swiper(accounts, selectedId: selectedId, margin: showArrows ? 8 : 32)
                if showArrows {
                    arrow("chevron.right", label: "Next account", enabled: index < accounts.count - 1) { send(.nextTapped) }
                }
            }
            if accounts.count > 1 {
                pageDots(count: accounts.count, selected: index)
            }
        }
        .focusable()
        .focusEffectDisabled()
        .onKeyPress(.leftArrow) { send(.previousTapped); return .handled }
        .onKeyPress(.rightArrow) { send(.nextTapped); return .handled }
        .onKeyPress(.return) { onOpenAccount(selectedId, false); return .handled }
    }

    private func swiper(_ accounts: [SavedAccount], selectedId: Int64, margin: CGFloat) -> some View {
        AccountSwiper(
            accounts: accounts,
            balances: balances,
            selectedId: selectedId,
            margin: margin,
            palette: palette,
            onSelect: { send(.accountSelected($0)) },
            onOpen: { onOpenAccount($0, false) }
        )
    }

    private func arrow(_ systemImage: String, label: String, enabled: Bool, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Image(systemName: systemImage)
                .font(.body.weight(.semibold))
                .frame(width: 40, height: 40)
                .background(Circle().fill(palette.secondaryContainer))
                .foregroundStyle(palette.onSecondaryContainer)
        }
        .buttonStyle(.plain)
        .disabled(!enabled)
        .opacity(enabled ? 1 : 0.4)
        .accessibilityLabel(label)
    }

    private func pageDots(count: Int, selected: Int) -> some View {
        HStack(spacing: 8) {
            ForEach(0..<count, id: \.self) { index in
                Capsule()
                    .fill(index == selected ? palette.primary : palette.outlineVariant)
                    .frame(width: index == selected ? 20 : 8, height: 8)
            }
        }
        .animation(.snappy, value: selected)
        .accessibilityElement()
        .accessibilityLabel("Account \(selected + 1) of \(count)")
    }

    // MARK: Activity

    private func recentActivity(_ saved: SavedAccount) -> some View {
        let activity = RecentActivity(saved)
        return VStack(alignment: .leading, spacing: 2) {
            HStack(alignment: .firstTextBaseline) {
                VStack(alignment: .leading, spacing: 2) {
                    Text("Recent activity")
                        .font(.headline)
                        .accessibilityAddTraits(.isHeader)
                    Text(saved.account.name)
                        .font(.footnote)
                        .foregroundStyle(palette.onSurfaceVariant)
                        .lineLimit(1)
                }
                Spacer()
                if case .entries = activity {
                    Button("See all") { onOpenAccount(saved.id, false) }
                        .font(.subheadline.weight(.semibold))
                        .foregroundStyle(palette.primary)
                }
            }
            switch activity {
            case let .entries(rows, currency):
                VStack(spacing: 0) {
                    ForEach(Array(rows.enumerated()), id: \.offset) { _, row in
                        HStack(spacing: 14) {
                            Image(systemName: "calendar")
                                .foregroundStyle(palette.onSecondaryContainer)
                                .frame(width: 40, height: 40)
                                .background(Circle().fill(palette.secondaryContainer))
                            Text(row.month)
                            Spacer()
                            Text("\(row.amount) \(currency)").monospacedDigit()
                        }
                        .padding(.vertical, 8)
                        .accessibilityElement(children: .combine)
                    }
                }
                .padding(.top, 8)
            case .noCheckIns:
                emptyActivity(systemImage: "calendar.badge.checkmark", message: "No check-ins yet") {
                    Button("Check in") { onOpenAccount(saved.id, true) }
                        .buttonStyle(.bordered)
                }
            case .noTrades:
                emptyActivity(systemImage: "list.bullet.rectangle", message: "No trades yet") { EmptyView() }
            }
        }
        .padding(20)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(RoundedRectangle(cornerRadius: 24, style: .continuous).fill(palette.surfaceContainerLow))
    }

    private func emptyActivity<Action: View>(systemImage: String, message: String, @ViewBuilder action: () -> Action) -> some View {
        VStack(spacing: 8) {
            Image(systemName: systemImage)
                .font(.title)
                .foregroundStyle(palette.onSurfaceVariant)
            Text(message)
                .font(.subheadline)
                .foregroundStyle(palette.onSurfaceVariant)
            action().padding(.top, 4)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 24)
    }

    // MARK: Empty and failure

    private func centered<Card: View>(@ViewBuilder _ card: () -> Card) -> some View {
        card()
            .frame(maxWidth: AdaptiveLayout.phoneColumnWidth)
            .padding(16)
            .frame(maxWidth: .infinity, maxHeight: .infinity)
    }

    private var addFirstAccountCard: some View {
        Button(action: onAddFirstAccount) {
            VStack(spacing: 6) {
                Image(systemName: "plus")
                    .font(.title3.weight(.semibold))
                    .foregroundStyle(palette.onPrimary)
                    .frame(width: 48, height: 48)
                    .background(Circle().fill(palette.primary))
                    .padding(.bottom, 6)
                Text("Add your first account").font(.headline)
                Text("Bank accounts, investments and loans").font(.footnote)
            }
            .foregroundStyle(palette.onPrimaryContainer)
            .frame(maxWidth: .infinity)
            .padding(.vertical, 32)
            .background(RoundedRectangle(cornerRadius: 24, style: .continuous).fill(palette.primaryContainer))
        }
        .buttonStyle(.plain)
    }

    private var loadFailedCard: some View {
        VStack(spacing: 12) {
            Image(systemName: "icloud.slash").font(.title2).foregroundStyle(palette.error)
            Text("Couldn't load your accounts").font(.headline)
            Button("Try again") { send(.retryTapped) }
                .buttonStyle(.borderedProminent)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 32)
        .background(RoundedRectangle(cornerRadius: 24, style: .continuous).fill(palette.surfaceContainerLow))
    }
}

/// Snapping row of cards; neighbours peek at the edges. The model owns the selection; the
/// scroll position is local so it can follow a selection whose card was only just added.
private struct AccountSwiper: View {
    let accounts: [SavedAccount]
    let balances: [Int64: Int64]
    let selectedId: Int64
    let margin: CGFloat
    let palette: FinancesColorScheme
    let onSelect: (Int64) -> Void
    let onOpen: (Int64) -> Void

    @State private var scrolledId: Int64?

    var body: some View {
        ScrollView(.horizontal) {
            LazyHStack(spacing: 12) {
                ForEach(accounts, id: \.id) { saved in
                    Button { onOpen(saved.id) } label: {
                        SavedAccountCardView(account: saved.account, balanceMinor: balances[saved.id] ?? 0, palette: palette)
                    }
                    .buttonStyle(.plain)
                    .accessibilityHint("Opens the account")
                    .containerRelativeFrame(.horizontal)
                    .id(saved.id)
                }
            }
            .scrollTargetLayout()
        }
        .contentMargins(.horizontal, margin, for: .scrollContent)
        .scrollTargetBehavior(.viewAligned)
        .scrollIndicators(.hidden)
        .scrollPosition(id: $scrolledId)
        .onAppear { scrolledId = selectedId }
        .onChange(of: selectedId) { _, id in
            guard id != scrolledId else { return }
            // Wait one run-loop turn so a card added in this same update is laid out first.
            DispatchQueue.main.async {
                withAnimation(.snappy) { scrolledId = id }
            }
        }
        .onChange(of: scrolledId) { _, id in
            if let id, id != selectedId { onSelect(id) }
        }
    }
}
