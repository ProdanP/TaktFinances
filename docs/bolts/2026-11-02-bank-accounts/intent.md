# Intent

## Problem

My Finances is an empty mock, so the person cannot record an account or see how much is on it. They need to add a Bank or Investment account—name, one of three currencies, and a current amount—and see those accounts listed on My Finances. The amount is money in every place they touch it: while typing, when it is saved, and when the list shows it.

## Done when

- My Finances lists each saved account with its name, its type (Bank or Investment), and its amount formatted as money in that account's currency.
- Until an account is saved, My Finances shows an empty list.
- A "+" button at the top right of My Finances opens Add Account on Android, desktop, and iOS.
- Add Account offers a type dropdown whose only choices are Bank and Investment.
- Choosing a type reveals a form with name, currency, and current amount. Bank and Investment use the same fields.
- Currency is a choice of Euro, US dollar, or Moldovan leu, and no other currency.
- Euro, US dollar, and Moldovan leu each use two decimal places. As the person types the amount in major units, the field shows grouping separators and those two decimal places. Separators follow the device locale. Changing the currency reformats the same amount for the new currency.
- Save parses that formatted amount into minor units and stores a `Long`. It does not store the formatted text. The list shows the same format, including the currency symbol.
- Save stores that account in the local database, returns to My Finances, and the new account appears in the list.
- Accounts belong to `:feature:accounts`. Compose lives in `:sharedUI` at `com.prodan.taktfinances.accounts`. SwiftUI lives in `iosApp/iosApp/Accounts/`.
