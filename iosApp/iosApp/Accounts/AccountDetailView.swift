import Charts
import SharedLogic
import SwiftUI

/// Account Detail: one scrolling column on compact width, header and chart beside the timeline on
/// regular width.
struct AccountDetailView: View {
    @Environment(\.colorScheme) private var colorScheme
    @Environment(\.colorSchemeContrast) private var colorSchemeContrast
    @Environment(\.horizontalSizeClass) private var horizontalSizeClass
    @State private var model: AccountDetailModel

    init(repository: AccountRepository, accountId: Int64, checkInOnOpen: Bool) {
        _model = State(initialValue: AccountDetailModel(repository: repository, accountId: accountId, checkInOnOpen: checkInOnOpen))
    }

    var body: some View {
        let palette = FinancesPalette.scheme(colorScheme: colorScheme, contrast: colorSchemeContrast)
        Group {
            switch model.state.content {
            case .loading:
                ProgressView().frame(maxWidth: .infinity, maxHeight: .infinity)
            case .failed:
                loadFailed(palette: palette)
            case let .loaded(loaded):
                if horizontalSizeClass == .regular {
                    HStack(alignment: .top, spacing: 32) {
                        ScrollView {
                            VStack(alignment: .leading, spacing: 24) {
                                header(loaded, palette: palette)
                                chart(loaded, palette: palette, height: 280)
                            }
                            .padding(.vertical, 8)
                        }
                        ScrollView {
                            activity(loaded, palette: palette, wide: true).padding(.vertical, 8)
                        }
                    }
                    .padding(.horizontal, 24)
                } else {
                    ScrollView {
                        VStack(alignment: .leading, spacing: 20) {
                            header(loaded, palette: palette)
                            chart(loaded, palette: palette, height: 170)
                            activity(loaded, palette: palette, wide: false)
                        }
                        .padding(.horizontal, 16)
                        .padding(.vertical, 8)
                        .frame(maxWidth: AdaptiveLayout.phoneColumnWidth)
                        .frame(maxWidth: .infinity)
                    }
                }
            }
        }
        .background(palette.background)
        .navigationTitle(loadedName)
        .navigationBarTitleDisplayMode(.inline)
        .onAppear { model.send(.shown) }
        .sheet(isPresented: sheetPresented) {
            if let sheet = model.state.sheet, case let .loaded(loaded) = model.state.content {
                CheckInSheetView(sheet: sheet, currency: loaded.currency, palette: palette, send: model.send)
                    .presentationDetents([.medium, .large])
            }
        }
    }

    private var loadedName: String {
        if case let .loaded(loaded) = model.state.content { return loaded.account.account.name }
        return ""
    }

    private var sheetPresented: Binding<Bool> {
        Binding(get: { model.state.sheet != nil }, set: { if !$0 { model.send(.sheetDismissed) } })
    }

    // MARK: Header

    private func header(_ loaded: LoadedAccount, palette: FinancesColorScheme) -> some View {
        let account = loaded.account.account
        let bankDetails = account.details as? AccountDetailsBankDetails
        return VStack(alignment: .leading, spacing: 6) {
            HStack(spacing: 12) {
                if let bank = bankDetails?.bank {
                    BankBadge(bank: bank, size: 40)
                } else {
                    Image(systemName: account.type.systemImage)
                        .frame(width: 40, height: 40)
                        .background(Circle().fill(palette.secondaryContainer))
                        .foregroundStyle(palette.onSecondaryContainer)
                        .accessibilityLabel(account.type.title)
                }
                VStack(alignment: .leading, spacing: 2) {
                    Text(AccountSummaryKt.accountSummary(details: account.details))
                        .font(.subheadline)
                    if let last4 = bankDetails?.last4, !last4.isEmpty {
                        Text("•••• \(last4)").font(.caption.monospacedDigit())
                    }
                }
                .foregroundStyle(palette.onSurfaceVariant)
            }
            HStack(alignment: .lastTextBaseline, spacing: 8) {
                Text(loaded.balance).font(.system(size: 44, weight: .regular, design: .rounded).monospacedDigit())
                Text(loaded.currency).font(.title3).foregroundStyle(palette.onSurfaceVariant)
            }
            .padding(.top, 6)
            .accessibilityElement(children: .ignore)
            .accessibilityLabel("Balance \(loaded.balance) \(loaded.currency)")
            HStack(spacing: 6) {
                Text("This month").foregroundStyle(palette.onSurfaceVariant)
                if let change = loaded.thisMonth {
                    changeLabel(change, withIcon: true, palette: palette)
                } else {
                    Text("—").foregroundStyle(palette.onSurfaceVariant)
                }
            }
            .font(.subheadline)
            .accessibilityElement(children: .ignore)
            .accessibilityLabel("This month: \(loaded.thisMonth?.spoken ?? "no previous month")")
        }
        .foregroundStyle(palette.onBackground)
    }

    private func changeLabel(_ change: ChangeUI, withIcon: Bool, palette: FinancesColorScheme) -> some View {
        let color: Color = switch change.direction {
        case .up: colorScheme == .dark ? Color(red: 0.49, green: 0.85, blue: 0.61) : Color(red: 0.12, green: 0.48, blue: 0.27)
        case .down: palette.error
        case .flat: palette.onSurfaceVariant
        }
        let icon = switch change.direction {
        case .up: "chart.line.uptrend.xyaxis"
        case .down: "chart.line.downtrend.xyaxis"
        case .flat: "minus"
        }
        return HStack(spacing: 4) {
            if withIcon { Image(systemName: icon) }
            Text(change.text).fontWeight(.medium).monospacedDigit()
        }
        .foregroundStyle(color)
    }

    // MARK: Chart

    private func chart(_ loaded: LoadedAccount, palette: FinancesColorScheme, height: CGFloat) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text("Balance").font(.headline)
                Spacer()
                Picker("Range", selection: Binding(get: { model.state.range }, set: { model.send(.rangeSelected($0)) })) {
                    ForEach(ChartRange.allCases, id: \.self) { Text($0.rawValue).tag($0) }
                }
                .pickerStyle(.segmented)
                .frame(width: 200)
            }
            BalanceChartView(points: model.state.chart, currency: loaded.currency, palette: palette)
                .frame(height: height)
            if model.state.chart.count == 1 {
                Text("The chart fills in as months pass")
                    .font(.footnote)
                    .foregroundStyle(palette.onSurfaceVariant)
            }
        }
        .padding(16)
        .background(RoundedRectangle(cornerRadius: 24, style: .continuous).fill(palette.surfaceContainerLow))
    }

    // MARK: Activity

    @ViewBuilder
    private func activity(_ loaded: LoadedAccount, palette: FinancesColorScheme, wide: Bool) -> some View {
        if let timeline = loaded.timeline {
            VStack(alignment: .leading, spacing: 8) {
                if !wide {
                    checkInButton.frame(maxWidth: .infinity).controlSize(.large).padding(.bottom, 12)
                }
                HStack {
                    Text("History").font(.headline).accessibilityAddTraits(.isHeader)
                    Spacer()
                    if wide { checkInButton }
                }
                VStack(spacing: 0) {
                    ForEach(timeline) { row in
                        if row.id != timeline.first?.id { Divider() }
                        timelineRow(row, palette: palette)
                    }
                }
                .background(RoundedRectangle(cornerRadius: 24, style: .continuous).fill(palette.surfaceContainerLow))
            }
        } else {
            VStack(spacing: 8) {
                Image(systemName: "list.bullet.rectangle").font(.title)
                Text("No trades yet").font(.subheadline)
            }
            .foregroundStyle(palette.onSurfaceVariant)
            .frame(maxWidth: .infinity)
            .padding(.vertical, 32)
            .background(RoundedRectangle(cornerRadius: 24, style: .continuous).fill(palette.surfaceContainerLow))
        }
    }

    private var checkInButton: some View {
        Button { model.send(.checkInTapped) } label: {
            Label("Check in", systemImage: "plus").frame(maxWidth: .infinity)
        }
        .buttonStyle(.borderedProminent)
        .fixedSize(horizontal: horizontalSizeClass == .regular, vertical: false)
    }

    private func timelineRow(_ row: TimelineRow, palette: FinancesColorScheme) -> some View {
        Button { model.send(.monthTapped(row.month)) } label: {
            HStack {
                VStack(alignment: .leading, spacing: 2) {
                    Text(row.label).foregroundStyle(row.checkedIn ? palette.onBackground : palette.onSurfaceVariant)
                    if !row.checkedIn {
                        Text("Not checked in").font(.caption).italic().foregroundStyle(palette.onSurfaceVariant)
                    }
                }
                Spacer()
                VStack(alignment: .trailing, spacing: 2) {
                    Text(row.balance)
                        .monospacedDigit()
                        .foregroundStyle(row.checkedIn ? palette.onBackground : palette.onSurfaceVariant)
                    if row.checkedIn {
                        if let change = row.change {
                            changeLabel(change, withIcon: false, palette: palette).font(.caption)
                        } else {
                            Text("Start").font(.caption).foregroundStyle(palette.onSurfaceVariant)
                        }
                    }
                }
                if !row.checkedIn {
                    Text("Check in").font(.subheadline.weight(.semibold)).foregroundStyle(palette.primary).padding(.leading, 8)
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(rowAccessibility(row))
        .accessibilityHint(row.checkedIn ? "Edits this month" : "Checks in for this month")
    }

    private func rowAccessibility(_ row: TimelineRow) -> String {
        guard row.checkedIn else { return "\(row.label), not checked in, \(row.balance)" }
        guard let change = row.change else { return "\(row.label), \(row.balance), start" }
        return "\(row.label), \(row.balance), \(change.spoken)"
    }

    private func loadFailed(palette: FinancesColorScheme) -> some View {
        VStack(spacing: 12) {
            Image(systemName: "icloud.slash").font(.title2).foregroundStyle(palette.error)
            Text("Couldn't load this account").font(.headline)
            Button("Try again") { model.send(.retryTapped) }.buttonStyle(.borderedProminent)
        }
        .frame(maxWidth: AdaptiveLayout.phoneColumnWidth)
        .padding(.vertical, 32)
        .frame(maxWidth: .infinity)
        .background(RoundedRectangle(cornerRadius: 24, style: .continuous).fill(palette.surfaceContainerLow))
        .padding(16)
        .frame(maxHeight: .infinity)
    }
}

/// Month-end balances as a line; tapping or dragging over a point labels its month and balance.
private struct BalanceChartView: View {
    let points: [ChartPoint]
    let currency: String
    let palette: FinancesColorScheme
    @State private var selected: Int?

    var body: some View {
        Chart {
            ForEach(points) { point in
                AreaMark(x: .value("Month", point.index), y: .value("Balance", point.balance))
                    .foregroundStyle(palette.primary.opacity(0.15))
                LineMark(x: .value("Month", point.index), y: .value("Balance", point.balance))
                    .foregroundStyle(palette.primary)
                    .lineStyle(StrokeStyle(lineWidth: 3, lineCap: .round, lineJoin: .round))
                PointMark(x: .value("Month", point.index), y: .value("Balance", point.balance))
                    .foregroundStyle(palette.primary)
                    .symbolSize(point.index == selected ? 90 : 40)
            }
            if let selected, let point = points.first(where: { $0.index == selected }) {
                RuleMark(x: .value("Month", point.index))
                    .foregroundStyle(palette.outline.opacity(0.6))
                    .lineStyle(StrokeStyle(lineWidth: 1, dash: [3, 3]))
                    .annotation(position: .top, overflowResolution: .init(x: .fit, y: .disabled)) {
                        VStack(alignment: .leading) {
                            Text(point.label).fontWeight(.semibold)
                            Text("\(point.balanceText) \(currency)")
                        }
                        .font(.caption)
                        .padding(6)
                        .background(RoundedRectangle(cornerRadius: 8).fill(palette.inverseSurface))
                        .foregroundStyle(palette.inverseOnSurface)
                    }
            }
        }
        .chartXScale(domain: -0.3...(Double(max(points.count - 1, 0)) + 0.3))
        .chartXAxis {
            AxisMarks(values: points.map(\.index)) { value in
                AxisValueLabel {
                    if let index = value.as(Int.self), let point = points.first(where: { $0.index == index }) {
                        Text(point.axisLabel)
                    }
                }
            }
        }
        .chartYScale(domain: .automatic(includesZero: false))
        .chartXSelection(value: $selected)
        .onChange(of: points.count) { selected = nil }
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(summary)
    }

    private var summary: String {
        guard let first = points.first, let last = points.last else { return "No balance yet" }
        if points.count == 1 { return "Balance for \(first.label): \(first.balanceText) \(currency)" }
        return "Balance, \(points.count) months: \(first.label) \(first.balanceText) \(currency) to \(last.label) \(last.balanceText) \(currency)"
    }
}

/// The check-in form shown in a sheet.
private struct CheckInSheetView: View {
    let sheet: CheckInSheetState
    let currency: String
    let palette: FinancesColorScheme
    let send: (AccountDetailIntent) -> Void
    @FocusState private var amountFocused: Bool

    var body: some View {
        NavigationStack {
            Form {
                Section {
                    Picker("Month", selection: Binding(get: { sheet.month }, set: { send(.sheetMonthSelected($0)) })) {
                        ForEach(sheet.months, id: \.label) { option in
                            Text(option.label).tag(option.month)
                        }
                    }
                    HStack {
                        TextField("0.00", text: Binding(get: { sheet.amountText }, set: { send(.amountChanged($0)) }))
                            .keyboardType(.decimalPad)
                            .focused($amountFocused)
                            .font(.title3.monospacedDigit())
                            .accessibilityLabel("Balance")
                        Text(currency).foregroundStyle(palette.onSurfaceVariant)
                    }
                } footer: {
                    Text("Enter this account's balance at the end of the month.")
                }
                if let hint = sheet.previousHint {
                    Section {
                        HStack {
                            Text(hint).foregroundStyle(palette.onSurfaceVariant)
                            Spacer()
                            Button { send(.sameAsLastMonthTapped) } label: {
                                Label("Same as last month", systemImage: "clock.arrow.circlepath")
                            }
                            .buttonStyle(.bordered)
                        }
                    }
                }
                if let error = sheet.error {
                    Section { Text(error.rawValue).foregroundStyle(palette.error) }
                }
                if sheet.canDelete {
                    Section {
                        Button("Delete", role: .destructive) { send(.deleteTapped) }.disabled(sheet.saving)
                    }
                }
            }
            .navigationTitle("Check in")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { send(.sheetDismissed) }.disabled(sheet.saving)
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Save") { send(.saveTapped) }.disabled(!sheet.canSave)
                }
            }
            .onChange(of: amountFocused) { _, focused in send(.amountFocusChanged(focused)) }
            .alert("Delete the check-in for \(sheet.monthLabel)?", isPresented: Binding(
                get: { sheet.confirmDelete },
                set: { if !$0 { send(.deleteCancelled) } }
            )) {
                Button("Delete", role: .destructive) { send(.deleteConfirmed) }
                Button("Cancel", role: .cancel) { send(.deleteCancelled) }
            }
        }
    }
}
