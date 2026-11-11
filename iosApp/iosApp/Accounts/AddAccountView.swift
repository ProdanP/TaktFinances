import SharedLogic
import SwiftUI

struct AddAccountView: View {
    var onClose: () -> Void = {}

    @Environment(\.colorScheme) private var colorScheme
    @Environment(\.colorSchemeContrast) private var colorSchemeContrast
    @State private var model: AddAccountModel
    @FocusState private var focus: FormField?

    init(repository: AccountRepository, onClose: @escaping () -> Void = {}) {
        self.init(model: AddAccountModel(repository: repository), onClose: onClose)
    }

    init(model: AddAccountModel, onClose: @escaping () -> Void = {}) {
        self.onClose = onClose
        _model = State(initialValue: model)
    }

    var body: some View {
        let palette = FinancesPalette.scheme(colorScheme: colorScheme, contrast: colorSchemeContrast)
        let state = model.state
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                previewCard(state, palette: palette)
                Picker("Type", selection: typeBinding) {
                    ForEach(state.types, id: \.name) { type in
                        Text(type.title).tag(type)
                    }
                }
                .pickerStyle(.segmented)
                .padding(.top, 16)

                sectionLabel("Name", isError: state.visibleErrors.contains(.name), palette: palette)
                textField("Salary, Savings, …", text: nameBinding, field: .name, palette: palette)
                    .textInputAutocapitalization(.sentences)

                detailsSection(state, palette: palette)
            }
            .padding(.horizontal, 16)
            .padding(.bottom, 24)
            .frame(maxWidth: 520)
            .frame(maxWidth: .infinity)
        }
        .scrollDismissesKeyboard(.interactively)
        .background(palette.background)
        .safeAreaInset(edge: .bottom) { saveBar(state, palette: palette) }
        .navigationTitle("New account")
        .navigationBarTitleDisplayMode(.inline)
        .navigationBarBackButtonHidden(true)
        .toolbar {
            ToolbarItem(placement: .cancellationAction) {
                Button("Cancel", action: onClose)
            }
        }
        .tint(palette.primary)
        .onChange(of: focus) { old, new in
            if let old { model.send(.fieldFocusChanged(old, false)) }
            if let new { model.send(.fieldFocusChanged(new, true)) }
        }
        .onChange(of: state.saved) { _, saved in
            if saved { onClose() }
        }
    }

    // MARK: Preview card

    private func previewCard(_ state: AddAccountState, palette: FinancesColorScheme) -> some View {
        VStack(alignment: .leading, spacing: 0) {
            HStack(spacing: 10) {
                if state.type == .bankaccount, let bank = state.details.bank {
                    BankBadge(bank: bank, size: 32)
                } else {
                    Image(systemName: state.type.systemImage)
                        .font(.system(size: 15, weight: .semibold))
                        .frame(width: 32, height: 32)
                        .background(Circle().fill(palette.onPrimary.opacity(0.2)))
                }
                Text(state.name.isEmpty ? "Account name" : state.name)
                    .font(.headline)
                    .foregroundStyle(palette.onPrimary.opacity(state.name.isEmpty ? 0.6 : 1))
                    .lineLimit(1)
                Spacer(minLength: 8)
                if state.type == .bankaccount, !state.details.last4.isEmpty {
                    Text("•••• \(state.details.last4)")
                        .font(.subheadline.monospacedDigit())
                        .foregroundStyle(palette.onPrimary.opacity(0.85))
                }
            }
            Text("Starting balance")
                .font(.caption)
                .foregroundStyle(palette.onPrimary.opacity(0.8))
                .padding(.top, 18)
            HStack(alignment: .center) {
                FilteredTextField(
                    placeholder: "",
                    prompt: Text("0.00").foregroundStyle(palette.onPrimary.opacity(0.45)),
                    value: amountBinding
                )
                .font(.system(size: 38, weight: .semibold, design: .rounded).monospacedDigit())
                .keyboardType(.decimalPad)
                .focused($focus, equals: .amount)
                .accessibilityLabel("Starting balance")
                currencyMenu(state, palette: palette)
            }
            Group {
                if state.visibleErrors.contains(.amount) {
                    Label("Enter an amount", systemImage: "exclamationmark.circle")
                        .foregroundStyle(palette.errorContainer)
                } else {
                    Text(state.summary ?? " ")
                        .foregroundStyle(palette.onPrimary.opacity(0.75))
                }
            }
            .font(.footnote)
            .lineLimit(1)
            .padding(.top, 2)
        }
        .foregroundStyle(palette.onPrimary)
        .padding(.horizontal, 20)
        .padding(.vertical, 18)
        .background(RoundedRectangle(cornerRadius: 24, style: .continuous).fill(palette.primary))
        .padding(.top, 8)
    }

    private func currencyMenu(_ state: AddAccountState, palette: FinancesColorScheme) -> some View {
        Menu {
            Picker("Currency", selection: currencyBinding) {
                ForEach(state.currencies, id: \.name) { currency in
                    Text(currency.code).tag(currency)
                }
            }
        } label: {
            HStack(spacing: 4) {
                Text(state.currency.code).font(.subheadline.weight(.semibold))
                Image(systemName: "chevron.up.chevron.down").font(.caption2.weight(.semibold))
            }
            .foregroundStyle(palette.onPrimary)
            .padding(.horizontal, 12)
            .padding(.vertical, 7)
            .background(Capsule().fill(palette.onPrimary.opacity(0.18)))
        }
        .accessibilityLabel("Currency \(state.currency.code)")
    }

    // MARK: Details

    @ViewBuilder
    private func detailsSection(_ state: AddAccountState, palette: FinancesColorScheme) -> some View {
        let errors = state.visibleErrors
        switch state.type {
        case .bankaccount:
            sectionLabel("Bank", isError: errors.contains(.bank), palette: palette)
            bankRow(state, palette: palette)
            fieldError("Choose a bank", show: errors.contains(.bank), palette: palette)

            sectionLabel("Account kind", isError: errors.contains(.accountkind), palette: palette)
            chips(state.bankAccountKinds, selected: state.details.bankAccountKind, title: \.title,
                  isError: errors.contains(.accountkind), palette: palette) { model.send(.bankAccountKindSelected($0)) }
            fieldError("Choose an account kind", show: errors.contains(.accountkind), palette: palette)

            sectionLabel("Last 4 digits · optional", isError: false, palette: palette)
            textField("0000", text: last4Binding, field: .last4, prefix: "••••", palette: palette)
                .keyboardType(.numberPad)
        case .investment:
            sectionLabel("Investment kind", isError: errors.contains(.investmentkind), palette: palette)
            chips(state.investmentKinds, selected: state.details.investmentKind, title: \.title,
                  isError: errors.contains(.investmentkind), palette: palette) { model.send(.investmentKindSelected($0)) }
            fieldError("Choose an investment kind", show: errors.contains(.investmentkind), palette: palette)

            sectionLabel("Platform", isError: false, palette: palette)
            textField("Binance, XTB, …", text: platformBinding, field: .platform, palette: palette)
        case .loan:
            sectionLabel("Direction", isError: errors.contains(.direction), palette: palette)
            Picker("Direction", selection: directionBinding) {
                ForEach(state.loanDirections, id: \.name) { direction in
                    Text(direction.title).tag(LoanDirection?.some(direction))
                }
            }
            .pickerStyle(.segmented)
            .sensoryFeedback(.selection, trigger: state.details.loanDirection)
            fieldError("Choose Borrowed or Lent", show: errors.contains(.direction), palette: palette)

            sectionLabel("Counterparty", isError: false, palette: palette)
            textField("Who the loan is with", text: counterpartyBinding, field: .counterparty, palette: palette)
        default:
            EmptyView()
        }
    }

    /// Horizontally scrolling bank badges; the last badges start off screen to show the row scrolls.
    private func bankRow(_ state: AddAccountState, palette: FinancesColorScheme) -> some View {
        ScrollView(.horizontal, showsIndicators: false) {
            LazyHStack(spacing: 4) {
                ForEach(state.banks, id: \.name) { bank in
                    let selected = bank == state.details.bank
                    Button { model.send(.bankSelected(bank)) } label: {
                        VStack(spacing: 6) {
                            BankBadge(bank: bank, size: 44)
                                .padding(4)
                                .overlay(Circle().strokeBorder(selected ? palette.primary : .clear, lineWidth: 2.5))
                            Text(bank.shortName)
                                .font(.caption)
                                .foregroundStyle(selected ? palette.primary : palette.onSurfaceVariant)
                                .lineLimit(1)
                        }
                        .frame(width: 76)
                    }
                    .buttonStyle(.plain)
                    .accessibilityAddTraits(selected ? [.isSelected] : [])
                }
            }
            .scrollTargetLayout()
            .padding(.vertical, 10)
            .padding(.horizontal, 6)
        }
        .scrollTargetBehavior(.viewAligned)
        .background(RoundedRectangle(cornerRadius: 12, style: .continuous).fill(palette.surfaceContainerLowest))
        .sensoryFeedback(.selection, trigger: state.details.bank)
    }

    private func chips<T: Hashable>(
        _ options: [T],
        selected: T?,
        title: KeyPath<T, String>,
        isError: Bool,
        palette: FinancesColorScheme,
        onSelect: @escaping (T) -> Void
    ) -> some View {
        FlowLayout(spacing: 8, lineSpacing: 8) {
            ForEach(options, id: \.self) { option in
                let isSelected = option == selected
                Button { onSelect(option) } label: {
                    Text(option[keyPath: title])
                        .font(.subheadline)
                        .padding(.horizontal, 14)
                        .padding(.vertical, 8)
                        .foregroundStyle(isSelected ? palette.onPrimary : palette.onSurface)
                        .background(Capsule().fill(isSelected ? palette.primary : palette.surfaceContainerLowest))
                        .overlay(Capsule().strokeBorder(isError ? palette.error : .clear, lineWidth: 1))
                }
                .buttonStyle(.plain)
                .accessibilityAddTraits(isSelected ? [.isSelected] : [])
            }
        }
        .sensoryFeedback(.selection, trigger: selected)
    }

    // MARK: Building blocks

    private func sectionLabel(_ text: String, isError: Bool, palette: FinancesColorScheme) -> some View {
        Text(text.uppercased())
            .font(.caption.weight(.medium))
            .kerning(0.4)
            .foregroundStyle(isError ? palette.error : palette.onSurfaceVariant)
            .padding(.top, 20)
            .padding(.bottom, 8)
    }

    private func textField(
        _ placeholder: String,
        text: Binding<String>,
        field: FormField,
        prefix: String? = nil,
        palette: FinancesColorScheme
    ) -> some View {
        let isError = model.state.visibleErrors.contains(field)
        return HStack(spacing: 8) {
            if let prefix {
                Text(prefix).foregroundStyle(palette.outlineVariant)
            }
            // Last 4 digits is filtered by the model, so it needs the mirroring field.
            Group {
                if field == .last4 {
                    FilteredTextField(placeholder: placeholder, value: text)
                } else {
                    TextField(placeholder, text: text)
                }
            }
            .focused($focus, equals: field)
            // Platforms and people are proper names; autocorrect would rewrite them ("Binance").
            .autocorrectionDisabled(field != .name)
        }
        .padding(.horizontal, 14)
        .frame(height: 46)
        .background(
            RoundedRectangle(cornerRadius: 12, style: .continuous)
                .fill(isError ? palette.errorContainer.opacity(0.18) : palette.surfaceContainerLowest)
        )
        .overlay(
            RoundedRectangle(cornerRadius: 12, style: .continuous)
                .strokeBorder(isError ? palette.error : .clear, lineWidth: 1.5)
        )
        .overlay(alignment: .bottomLeading) {
            if isError, let message = errorText(field) {
                Label(message, systemImage: "exclamationmark.circle")
                    .font(.footnote)
                    .foregroundStyle(palette.error)
                    .offset(y: 22)
            }
        }
        .padding(.bottom, isError ? 22 : 0)
    }

    /// Text-field errors are drawn by `textField`; other fields use `fieldError`.
    private func errorText(_ field: FormField) -> String? {
        switch field {
        case .name: "Enter a name"
        case .last4: "Enter exactly 4 digits"
        case .platform: "Enter a platform"
        case .counterparty: "Enter who the loan is with"
        default: nil
        }
    }

    @ViewBuilder
    private func fieldError(_ text: String, show: Bool, palette: FinancesColorScheme) -> some View {
        // Choice groups only; text fields draw their own error in `textField`.
        if show {
            Label(text, systemImage: "exclamationmark.circle")
                .font(.footnote)
                .foregroundStyle(palette.error)
                .padding(.top, 6)
        }
    }

    private func saveBar(_ state: AddAccountState, palette: FinancesColorScheme) -> some View {
        VStack(spacing: 8) {
            if let hint = state.hint {
                Label(hint, systemImage: "info.circle")
                    .font(.footnote)
                    .foregroundStyle(palette.onSurfaceVariant)
                    .multilineTextAlignment(.center)
            }
            Button { model.send(.saveTapped) } label: {
                Text("Add account")
                    .font(.headline)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 8)
            }
            .buttonStyle(.borderedProminent)
            .buttonBorderShape(.capsule)
            .controlSize(.large)
            .disabled(!state.canSave)
            .accessibilityHint(state.hint ?? "")
        }
        .padding(.horizontal, 16)
        .padding(.top, 10)
        .padding(.bottom, 8)
        .frame(maxWidth: 520)
        .frame(maxWidth: .infinity)
        .background(.ultraThinMaterial)
    }

    // MARK: Bindings

    private var typeBinding: Binding<AccountType> {
        Binding(get: { model.state.type }, set: { model.send(.typeSelected($0)) })
    }

    private var currencyBinding: Binding<Currency> {
        Binding(get: { model.state.currency }, set: { model.send(.currencySelected($0)) })
    }

    private var nameBinding: Binding<String> {
        Binding(get: { model.state.name }, set: { model.send(.nameChanged($0)) })
    }

    private var amountBinding: Binding<String> {
        Binding(get: { model.state.amountText }, set: { model.send(.amountChanged($0)) })
    }

    private var last4Binding: Binding<String> {
        Binding(get: { model.state.details.last4 }, set: { model.send(.last4Changed($0)) })
    }

    private var platformBinding: Binding<String> {
        Binding(get: { model.state.details.platform }, set: { model.send(.platformChanged($0)) })
    }

    private var directionBinding: Binding<LoanDirection?> {
        Binding(get: { model.state.details.loanDirection }, set: { if let v = $0 { model.send(.loanDirectionSelected(v)) } })
    }

    private var counterpartyBinding: Binding<String> {
        Binding(get: { model.state.details.counterparty }, set: { model.send(.counterpartyChanged($0)) })
    }
}

private extension AccountType {
    var systemImage: String {
        switch self {
        case .bankaccount: "building.columns"
        case .investment: "chart.line.uptrend.xyaxis"
        default: "arrow.left.arrow.right"
        }
    }
}

/// Monogram badge; replace the body with a logo image when real assets are added.
struct BankBadge: View {
    let bank: Bank
    var size: CGFloat = 24

    var body: some View {
        let argb = UInt32(truncatingIfNeeded: bank.colorArgb)
        Text(bank.initials)
            .font(.system(size: size * 0.4, weight: .bold))
            .foregroundStyle(.white)
            .frame(width: size, height: size)
            .background(
                Circle().fill(
                    Color(
                        red: Double((argb >> 16) & 0xFF) / 255,
                        green: Double((argb >> 8) & 0xFF) / 255,
                        blue: Double(argb & 0xFF) / 255
                    )
                )
            )
    }
}

// MARK: Previews

private func previewModel(_ intents: [AddAccountIntent]) -> AddAccountModel {
    let model = AddAccountModel(repository: AccountsFactoryKt.createAccountRepository())
    intents.forEach(model.send)
    return model
}

#Preview("Empty") {
    NavigationStack { AddAccountView(model: previewModel([])) }
}

#Preview("Bank filled") {
    NavigationStack {
        AddAccountView(model: previewModel([
            .nameChanged("Salary"),
            .amountChanged("2500"),
            .bankSelected(.victoriabank),
            .bankAccountKindSelected(.savings),
            .last4Changed("0421"),
        ]))
    }
}

#Preview("Investment filled") {
    NavigationStack {
        AddAccountView(model: previewModel([
            .typeSelected(.investment),
            .currencySelected(.usd),
            .nameChanged("Crypto stash"),
            .amountChanged("1250"),
            .investmentKindSelected(.crypto),
            .platformChanged("Binance"),
        ]))
    }
}
