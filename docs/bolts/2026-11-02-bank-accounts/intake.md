# Intake

## Depth

feature

## Summary

This bolt fills the My Finances destination, which is an empty mock today, with a list of accounts and the amount on each. A "+" button at the top right opens Add Account. There the person chooses Bank or Investment, then enters a name, one of Euro, US dollar, or Moldovan leu, and the current amount. The amount is formatted while typing, stored as minor units, and shown formatted on the list. Save stores the account in the app's local database, and My Finances shows it. Domain and SQLDelight live in a new accounts feature; Compose lives in `:sharedUI` and SwiftUI lives in `iosApp`.
