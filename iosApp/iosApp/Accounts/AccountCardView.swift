import SharedLogic
import SwiftUI

/// The account card layout shared by the Add Account preview and the My Finances swiper:
/// badge, name, optional last 4 digits, an amount row and a footer line. The amount row is a
/// slot, so the preview can put its editable amount and currency menu there.
struct AccountCardView<Amount: View, Footer: View>: View {
    let type: AccountType
    let bank: Bank?
    let name: String
    let last4: String?
    let amountLabel: String
    let palette: FinancesColorScheme
    @ViewBuilder let amount: () -> Amount
    @ViewBuilder let footer: () -> Footer

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            HStack(spacing: 10) {
                if type == .bankaccount, let bank {
                    BankBadge(bank: bank, size: 32)
                } else {
                    Image(systemName: type.systemImage)
                        .font(.system(size: 15, weight: .semibold))
                        .frame(width: 32, height: 32)
                        .background(Circle().fill(palette.onPrimary.opacity(0.2)))
                        .accessibilityLabel(type.title)
                }
                Text(name.isEmpty ? "Account name" : name)
                    .font(.headline)
                    .foregroundStyle(palette.onPrimary.opacity(name.isEmpty ? 0.6 : 1))
                    .lineLimit(1)
                Spacer(minLength: 8)
                if type == .bankaccount, let last4, !last4.isEmpty {
                    Text("•••• \(last4)")
                        .font(.subheadline.monospacedDigit())
                        .foregroundStyle(palette.onPrimary.opacity(0.85))
                }
            }
            Text(amountLabel)
                .font(.caption)
                .foregroundStyle(palette.onPrimary.opacity(0.8))
                .padding(.top, 18)
            HStack(alignment: .center) { amount() }
            footer()
                .font(.footnote)
                .lineLimit(1)
                .padding(.top, 2)
        }
        .foregroundStyle(palette.onPrimary)
        .padding(.horizontal, 20)
        .padding(.vertical, 18)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(RoundedRectangle(cornerRadius: 24, style: .continuous).fill(palette.primary))
    }
}

/// A saved account as a read-only card.
struct SavedAccountCardView: View {
    let account: FinanceAccount
    let palette: FinancesColorScheme

    var body: some View {
        let bankDetails = account.details as? AccountDetailsBankDetails
        AccountCardView(
            type: account.type,
            bank: bankDetails?.bank,
            name: account.name,
            last4: bankDetails?.last4,
            amountLabel: "Balance",
            palette: palette
        ) {
            Text(AmountInputKt.formatAmount(minor: account.amountMinor))
                .font(.system(size: 38, weight: .semibold, design: .rounded).monospacedDigit())
                .lineLimit(1)
                .minimumScaleFactor(0.6)
            Text(account.currency.code)
                .font(.headline)
                .foregroundStyle(palette.onPrimary.opacity(0.85))
            Spacer(minLength: 0)
        } footer: {
            Text(AccountSummaryKt.accountSummary(details: account.details))
                .foregroundStyle(palette.onPrimary.opacity(0.75))
        }
        .accessibilityElement(children: .combine)
    }
}
