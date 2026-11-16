# Takt Finances: Business Requirements Document

| | |
|---|---|
| Status | Draft v0.1, open for review |
| Last updated | 2026-11-16 |
| Scope | Version 1 (v1) and the Phase 2 backlog |
| Platforms | Android, iOS, desktop (Windows, macOS, Linux) |

This is a living document. Decisions are numbered (D1, D2, …) so later changes can refer to them. Items marked **Proposed** are working defaults that have not been confirmed yet. Items marked **Open** need a decision before the feature that depends on them is built. Detailed behavior moves into OpenSpec specs (`openspec/specs/`) one change at a time; when a spec and this document disagree, the spec wins and this document is updated.

---

## 1. Purpose and vision

Takt Finances gives a person **one clear view of all their money**: bank accounts, investments and loans, in several currencies, and how the total changes month by month.

Most finance apps assume you will log every transaction. Many people never do, but they would happily write down "this is what I have" once a month. Takt Finances supports both styles, **per account**: track your everyday card in detail and your brokerage account with one number a month, and still see a single combined picture.

### 1.1 Goals for v1

1. A user can record every account they own, in its own currency, in under a minute per account.
2. A user can keep each account up to date in the way that suits it: by logging transactions or by entering a month-end balance.
3. A user can see their total net worth in the currency they think in, and whether it went up or down each month.
4. Everything works offline and stays on the device.

### 1.2 Non-goals for v1

- Bank connections, automatic import, or reading SMS/notifications.
- Budgets, spending limits, goals.
- Multiple users, sharing, sign-in or sync between devices.
- Investment performance analysis beyond net change (see D4).
- Live market prices for stocks, ETFs or crypto (see D14).

### 1.3 Target users

| Persona | Description | What they need |
|---|---|---|
| **The monthly checker** | Has salary, savings and some investments. Doesn't want to log every coffee. | Enter balances once a month, see the trend. |
| **The detail tracker** | Wants to know where money goes on their main card. | Log transactions with categories, see spending per category. |
| **The mixed user** (most common) | Tracks the main card in detail, everything else monthly. | Both modes side by side, one combined total. |

Primary market: Moldova (MDL is the default currency, local banks are listed, official rates come from the National Bank of Moldova), with users who hold money in USD, EUR, GBP and RON.

---

## 2. Decision log

| # | Decision |
|---|---|
| D1 | Every Bank and Loan account has a **tracking mode**: **Transactions** or **Monthly**. Investment accounts are always tracked by trades (D16). |
| D2 | Switching an account's mode **freezes the past history as read-only**; the new mode starts on the day of the switch, from the account's current balance. |
| D3 | **Monthly** is the default tracking mode for new Bank and Loan accounts. |
| D4 | **Gain/loss means net change only**: balance at the end of a period minus balance at the end of the previous period. No contributions or performance split. |
| D5 | The user picks a **Main Currency** in Settings (MDL by default). All combined values are shown in it. |
| D6 | Exchange rates are the **official National Bank of Moldova (BNM) rates**, fetched by the app and kept for offline use. |
| D7 | **Local only, no sign-in.** Data stays on the device. |
| D8 | **Guided 4-step onboarding**: Welcome, Main Currency, How tracking works, First account. |
| D9 | Transactions use a **fixed list of categories**, separate for money in and money out. |
| D10 | Account Detail shows a **type-specific block**: deposit info, In vs Out, total change, loan original amount and progress, loan due date and overdue, Mark as repaid. |
| D11 | v1 has **no backup, export, archive, month-end reminder or app lock**. These move to Phase 2. Data loss on a lost or reset device is an accepted v1 risk. |
| D12 | The third Home tab is renamed from **Profile** to **Settings**. |
| D13 | A Loan transaction is a **Repayment** (lowers the balance) or an **Additional amount** (raises it). Loans have no categories. |
| D14 | In Transactions mode, an Investment account records **trades** (Buy/Sell, instrument, quantity, price). Its value = quantity held x the **latest price the user entered** for each instrument. No live market prices. |
| D15 | Investment accounts track **holdings only**, not the cash held at the broker. |
| D16 | Investment accounts are **always tracked by trades** (Transactions mode). They have no Monthly mode and no mode switch. |
| D17 | Entries can't be dated **before the account was created** (monthly entries from the creation month onward). |
| D18 | When an account's mode is switched twice on the same day, the period that lasted less than a day is **discarded**. |
| D19 | Combined totals are shown **only on the Dashboard**, not on My Finances. |
| D20 | v1 is in **English only**. |
| D21 | The platform's **automatic device backup** (Android Auto Backup, iCloud device backup) stays enabled for the app's data. |
| D22 | The app opens on **Dashboard**, or on My Finances while there are no accounts. |
| D23 | A trade has an optional **fee**. It is shown on the trade and summed per account, but doesn't change the holdings value (no cash is tracked, D15). |
| D24 | **No data migration**: v1 starts with a fresh database; accounts saved by development builds are not kept. |

---

## 3. Key concepts and glossary

| Term | Meaning |
|---|---|
| **Account** | Something that holds or owes money: a bank account, an investment, or a loan. Has a name, a type, a currency, type-specific details and a tracking mode. |
| **Account type** | Bank, Investment or Loan. Decides which details and which Account Detail block are shown. |
| **Tracking mode** | How the account's balance is kept up to date: **Transactions** or **Monthly** (D1). |
| **Transaction** | A single entry in a Transactions-mode account. Its shape depends on the type: a Bank transaction (amount, In/Out, category), a Loan repayment or additional amount, or an Investment trade. |
| **Trade** | A Buy or Sell of an instrument in an Investment account: instrument, quantity, price per unit, date. |
| **Instrument** | What is bought or sold in an investment, named by the user, e.g. "VWCE" or "Bitcoin". |
| **Holding** | The quantity of one instrument currently held in an Investment account, together with its latest price. |
| **Price update** | A price for an instrument that the user entered on a date without trading, so the holding's value stays current. |
| **Monthly entry** | The balance of a Monthly-mode account for one calendar month, e.g. "November 2026: 11,500.00". |
| **Opening balance** | The balance a Transactions-mode period starts from. |
| **Period** | A stretch of an account's history kept in one tracking mode. An account has one active period and zero or more **frozen periods** (D2). |
| **Frozen period** | A past period from before a mode switch. Read-only. |
| **Month-end balance** | An account's balance on the last day of a month. Derived the same way for both modes (§9.4); it is what the Dashboard and all charts use. |
| **Main Currency** | The currency combined values are shown in (D5). |
| **Net worth** | Bank + Investment + Lent loans − Borrowed loans, all converted to the Main Currency. |
| **Net change** | Month-end balance minus the previous month-end balance (D4). |

### 3.1 Money rules (apply everywhere)

- Supported currencies: **MDL, USD, EUR, GBP, RON**, each with exactly 2 decimal places.
- Amounts are stored as whole minor units (bani, cents, pence) so no rounding errors build up.
- Amounts are shown with thousands separators and 2 decimals, e.g. `2,500.00 MDL`.
- Converted values are shown with an "≈" prefix, e.g. `≈ 21,240.00 MDL`.
- Positive changes are shown in a success color with a `+` sign, negative ones in an error color with a `−` sign, and zero as neutral `0.00`. Color is never the only signal.

---

## 4. Scope

### 4.1 v1 scope

| Area | Included |
|---|---|
| Onboarding | 4 steps (D8) |
| Home | Three tabs: Dashboard, My Finances, Settings (renamed from Profile, D12) |
| Accounts | Add (already built), view, edit, delete, tracking mode choice and switch |
| Monthly mode | Monthly check-in, edit past months, carry-forward for missing months |
| Transactions mode | Bank: transactions with fixed categories. Loan: repayments and additional amounts. Investment: trades, holdings, price updates |
| Account Detail | Header, chart, timeline, type-specific block (D10) |
| Dashboard | Net worth, monthly and year-to-date change, net worth chart, allocation by type, top movers |
| Currency | Main Currency setting, BNM rates with offline cache |
| Settings | Main Currency, exchange rates (view and refresh), about |

### 4.2 Phase 2 backlog

Backup and restore · CSV export · live market prices · dividends and broker cash for investments · Romanian and Russian translations · archive accounts · month-end reminder notification · app lock (PIN/biometric) · transfers between own accounts · user-defined categories · contributions and performance for investments · credit card limit and usage · best/worst month · sync between devices · erase all data.

### 4.3 Already delivered (baseline)

| Capability | Where |
|---|---|
| Landing screen with Continue to Home | `feature/onboarding` |
| Home with Dashboard, My Finances, Profile tabs (adaptive bar / rail / drawer) | `feature/home` |
| Add Account form: type, name, currency, amount, type details, live preview, validation | spec `finance-account-creation`, `finance-account-details` |
| Accounts stored locally, My Finances card swiper, empty state, load failure, adaptive layouts | change `persist-accounts-and-card-swiper` (in progress) |

---

## 5. Information architecture

```
First launch only
+---------+   +---------------+   +--------------------+   +-------------------+
| Welcome |-->| Main Currency |-->| How tracking works |-->| Add first account |--+
+---------+   +---------------+   +--------------------+   +-------------------+  |
                                                                                  v
+---------------------------------------------------------------------------------+
| Home                                                                            |
|  +-------------+   +------------------------------+   +----------------------+  |
|  | Dashboard   |   | My Finances                  |   | Settings             |  |
|  |  net worth  |   |  account cards (swiper)  [+] |   |  Main Currency       |  |
|  |  changes    |   |  recent activity             |   |  Exchange rates      |  |
|  |  charts     |   |    |                         |   |  About               |  |
|  |  allocation |   |    v                         |   +----------------------+  |
|  |  top movers |   |  Account Detail              |                             |
|  +-------------+   |    - Monthly check-in        |                             |
|                    |    - Log transaction         |                             |
|                    |    - Edit / Delete account   |                             |
|                    |    - Switch tracking mode    |                             |
|                    |  Add Account                 |                             |
|                    +------------------------------+                             |
+---------------------------------------------------------------------------------+
```

**Navigation rules**

- Home chrome adapts to the window, as today: bottom bar on phones, navigation rail on medium widths, permanent drawer on wide windows (iOS: TabView / NavigationSplitView).
- On launch the app opens on **Dashboard**; if the user has no accounts yet it opens on **My Finances** (D22).
- The selected tab is kept while the app is running.
- The third tab is called **Settings** (D12). This renames today's Profile tab, so the existing `finance-account-creation` scenario that mentions "Dashboard or Profile" needs updating.

---

## 6. User journeys

Each journey lists the trigger, the main steps, and the expected result. Acceptance criteria are in the feature sections (§7 to §12) and use the same WHEN/THEN style as the OpenSpec specs.

| # | Journey | Trigger | Steps | Result |
|---|---|---|---|---|
| J0 | First launch | User opens the app for the first time | Welcome, pick Main Currency, read how tracking works, add a first account or skip | Lands in Home; onboarding never shows again |
| J1 | Add an account | Taps "+" on My Finances, or the empty-state card | Picks type, name, currency, amount, details, tracking mode; saves | New card is selected on My Finances |
| J2 | Monthly check-in | Opens a Monthly-mode account | Taps "Check in", enters this month's balance (or "Same as last month"); saves | Monthly entry saved; balance, change and charts update |
| J3 | Log a transaction | Opens a Transactions-mode account | Taps "Add transaction" (Bank: amount, In/Out, category; Loan: repayment or additional amount; Investment: a Buy/Sell trade); saves | Balance or value and charts update |
| J3b | Update investment prices | Opens a Investment account | Taps "Update prices", edits the latest price of any holding; saves | Holdings and account value update |
| J4 | Switch tracking mode (Bank, Loan) | Account Detail, tracking mode toggle | Picks the other mode, confirms | Old history frozen; new mode starts today from the current balance |
| J5 | Review an account | Taps a card on My Finances | Sees header, chart, timeline, type block | Understands the account's state and trend |
| J6 | Edit or delete an account | Account Detail menu | Edits allowed fields and saves, or deletes after confirming | Account updated, or removed with all its history |
| J7 | Review net worth | Opens Dashboard | Reads net worth, monthly change, chart, allocation, top movers | Knows whether total money went up or down |
| J8 | Change Main Currency | Settings, Main Currency | Picks another currency | All combined values shown in the new currency |
| J9 | Check exchange rates | Settings, Exchange rates | Reads today's rates; optionally refreshes | Knows which rates the totals use |
| J10 | Close a loan | Loan Account Detail | Taps "Mark as repaid", confirms | Loan balance becomes 0 and it shows as Repaid |

---

## 7. Onboarding (J0)

Replaces today's single Landing screen (D8).

### FR-ON-1 Onboarding shows on first launch only
- WHEN the app is opened for the first time, THEN step 1 (Welcome) is shown.
- WHEN the user has finished step 4 (by saving an account or skipping), THEN onboarding is never shown again, even after a restart.
- WHEN the app is closed before step 4 is finished, THEN onboarding starts again from step 1 on the next launch, and any choice already saved (Main Currency) is kept and preselected.

### FR-ON-2 Step 1: Welcome
Shows the app name, a one-line value statement, an illustration and a **Get started** button.
- WHEN the user taps Get started, THEN step 2 is shown.

### FR-ON-3 Step 2: Main Currency
Shows MDL, USD, EUR, GBP and RON as single-select options, with MDL preselected, plus **Continue** and **Skip**.
- WHEN the user selects EUR and taps Continue, THEN EUR is saved as the Main Currency and step 3 is shown.
- WHEN the user taps Skip, THEN the Main Currency stays MDL and step 3 is shown.
- The screen says the choice can be changed later in Settings.

### FR-ON-4 Step 3: How tracking works
Shows two explanatory cards, nothing to choose:
- **Monthly** (labelled "Recommended"): "Enter your balance once a month. Quick and simple."
- **Transactions**: "Log every payment and income. See where money goes."
And: "You choose per account, and you can switch later. Investments are always tracked by trades." Plus a **Continue** button.
- WHEN the user taps Continue, THEN step 4 is shown.

### FR-ON-5 Step 4: Add your first account
Shows the Add Account form (§8.1) with Monthly preselected, and a **Skip** action.
- WHEN the user saves a valid account, THEN Home opens on My Finances with that account's card selected.
- WHEN the user taps Skip, THEN Home opens on My Finances showing the "Add your first account" empty state.

### FR-ON-6 Back navigation
- WHEN the user goes back from steps 2, 3 or 4, THEN the previous step is shown with earlier choices kept.
- WHEN the user goes back from step 1, THEN the app closes (Android) or nothing happens (iOS, desktop).

---

## 8. Accounts

### 8.1 Add Account (J1)

The existing form stays as specified in `finance-account-creation` and `finance-account-details` (type, name, currency, amount, details, live preview, missing-fields hint, field errors). v1 adds:

#### FR-AC-1 Tracking mode choice
For Bank and Loan, the form shows a **Tracking** choice with two options, **Monthly** and **Transactions**, with Monthly preselected (D3). A one-line hint under it explains the selected mode.

For Investment, the tracking choice is hidden and replaced by the line "Investments are tracked by trades" (D16).
- WHEN the user selects Investment, THEN the tracking choice is hidden; WHEN they switch back to Bank or Loan, THEN the tracking choice they had before is shown again.
- WHEN the Add Account screen opens, THEN Monthly is selected.
- WHEN the user switches account type, THEN the tracking choice is kept.
- WHEN the user saves, THEN the account is created with the selected mode.

#### FR-AC-2 Meaning of the amount
The amount label depends on the tracking mode:
- Monthly: **"Balance this month"**. Saving creates the monthly entry for the current month with this amount.
- Transactions: **"Current balance"**. Saving creates the opening balance, dated today.
- **Investment**: no amount field. The form says "You'll add your holdings as trades after saving", and the account starts at 0.00 (§9.2.2).
- WHEN the user creates a Monthly account with 2,500.00 MDL in November 2026, THEN the account has one monthly entry, November 2026: 2,500.00.
- WHEN the user creates a Transactions account with 2,500.00 MDL on 16 Nov 2026, THEN the account's opening balance is 2,500.00 on 16 Nov 2026 and it has no transactions.

#### FR-AC-3 New optional detail fields (for D10)

| Type | Field | Shown when | Rules |
|---|---|---|---|
| Bank | Interest rate (% per year) | Kind = Deposit | Optional; 0 to 100; up to 2 decimals |
| Bank | Maturity date | Kind = Deposit | Optional; date picker |
| Loan | Original amount | Always | Optional; ≥ the amount entered |
| Loan | Due date | Always | Optional; date picker |

- WHEN a Bank account's kind is not Deposit, THEN interest rate and maturity date are not shown, and switching kind away from Deposit clears them.
- WHEN a Loan's original amount is less than its amount, THEN "Add account" is disabled and, after the user leaves the field, it shows "Must be at least the current amount".
- These fields never appear in the missing-fields hint, because they are optional.

### 8.2 My Finances

Baseline: the card swiper, page indicator, empty state, load failure and adaptive layouts from `persist-accounts-and-card-swiper`. v1 adds:

#### FR-MF-1 Main Currency equivalent on cards
- WHEN an account's currency differs from the Main Currency and a rate is available, THEN its card shows the converted value as a second line, e.g. `1,200.00 USD` and `≈ 21,240.00 MDL`.
- WHEN the account's currency is the Main Currency, THEN no second line is shown.
- WHEN no rate is available, THEN the second line reads "Rate unavailable".

#### FR-MF-2 No combined total
My Finances shows no combined total; combined values live only on the Dashboard (D19).

#### FR-MF-3 Recent activity shows real entries
The "Recent activity" area below the selected card shows the latest 5 entries of the account's active period: transactions for Transactions mode, monthly entries for Monthly mode, newest first, plus a **See all** action that opens Account Detail.
- WHEN the selected account has no entries besides its starting balance, THEN the area shows "No transactions yet" (Transactions), "No trades yet" (Investment) or "No check-ins yet" (Monthly) with the matching action button.

#### FR-MF-4 Open Account Detail
- WHEN the user taps a card, THEN Account Detail for that account opens.

#### FR-MF-5 Repaid loans sort last
- WHEN a loan is marked as repaid (§10.3), THEN its card shows a "Repaid" badge and moves after all other cards.

### 8.3 Edit an account (J6)

#### FR-AC-4 What can be edited
Account Detail offers **Edit**, which opens the Add Account form filled with the account's values, titled "Edit account", with the action "Save".

| Field | Editable? |
|---|---|
| Name, type details, optional detail fields | Always |
| Type | Only while the account has no entries besides its starting balance |
| Currency | Only while the account has no entries besides its starting balance |
| Amount | Not on this form; balances change through check-ins or transactions |
| Tracking mode | Not on this form; changed with the mode switch (§9.3) |

- WHEN the account has entries and the user opens Edit, THEN type and currency are shown read-only with the hint "Can't be changed once the account has history".
- WHEN the user saves valid changes, THEN Account Detail shows the updated values.

### 8.4 Delete an account (J6)

#### FR-AC-5 Delete with confirmation
- WHEN the user chooses Delete on Account Detail, THEN a dialog says "Delete "<name>"? This removes the account and all its history, including past months on the Dashboard. This can't be undone." with **Delete** (destructive style) and **Cancel**.
- WHEN the user confirms, THEN the account and all its entries are removed, My Finances is shown, and the nearest neighbouring card is selected.
- WHEN the user cancels, THEN nothing changes.

---

## 9. Tracking modes

### 9.1 Monthly mode (J2)

#### FR-MO-1 Check in for a month
Account Detail of a Monthly account shows a **Check in** action. It opens a sheet with:
- the month (current month by default; can be changed to any earlier month of the active period, never a future one and never before the month the account was created, D17)
- the balance amount (same input rules as the Add Account amount)
- the previous month's balance as a hint, e.g. "October: 10,000.00"
- a **Same as last month** shortcut that fills in the previous month's balance

Acceptance:
- WHEN the account's latest entry is October 2026 with 10,000.00 and the user checks in for November with 11,500.00, THEN November 2026: 11,500.00 is saved, the balance shows 11,500.00 and the change for November shows +1,500.00 (+15.0%).
- WHEN the user taps Same as last month, THEN the amount is filled with October's balance and can still be edited.
- WHEN the user picks a month that already has an entry, THEN the sheet shows that entry's amount and saving replaces it (one entry per month; the last save wins).
- WHEN the user tries to pick a month after the current month, or before the active period's start month, THEN it isn't offered.
- Save is enabled only when the amount holds a number.

#### FR-MO-2 Edit and delete monthly entries
- WHEN the user taps a monthly entry in the active period, THEN the check-in sheet opens for that month with its amount.
- WHEN the user deletes a monthly entry, THEN a confirmation is shown, and after confirming the entry is removed and later months' changes are recalculated.
- The month the period started in (its starting balance) can be edited but not deleted.
- Entries in a frozen period can't be edited or deleted.

#### FR-MO-3 Missing months
- WHEN a month between two entries (or between the latest entry and the current month) has no entry, THEN its month-end balance is the previous entry's balance (carry-forward), and the timeline shows the month with a muted "Not checked in" label and a **Check in** shortcut.
- A missing month is never counted as 0.

#### FR-MO-4 Monthly timeline
Shows one row per month, newest first, from the current month back to the start of the account: month, balance, and change vs the previous month (abs and %). The % is hidden when the previous balance is 0.

### 9.2 Transactions mode (J3)

What a transaction looks like depends on the account type: **Bank** accounts log money in and out with categories (FR-TX-1), **Investment** accounts log trades (§9.2.2), and **Loan** accounts log repayments (§9.2.3).

#### FR-TX-1 Add a transaction (Bank)
Account Detail of a Transactions-mode Bank account shows an **Add transaction** action. It opens a form with:

| Field | Rules |
|---|---|
| Amount | Required; > 0; same input rules as the Add Account amount; no sign is typed |
| Direction | **Out** / **In** toggle; Out selected by default |
| Category | Required; from the list for the chosen direction (§9.2.1) |
| Date | Today by default; no future dates; not before the active period's start date |
| Note | Optional; up to 200 characters |

- WHEN the user enters 420.00, Out, Groceries, today, and saves, THEN the balance drops by 420.00 and the transaction is first in the timeline.
- WHEN the user switches direction, THEN the selected category is cleared if it isn't in the new direction's list.
- Save is enabled only when the amount is > 0 and a category is chosen; otherwise a missing-fields hint is shown, as on Add Account.

#### 9.2.1 Categories (D9)

| Direction | Categories |
|---|---|
| Out | Groceries, Restaurants, Transport, Housing, Utilities, Health, Shopping, Entertainment, Education, Travel, Fees, Transfer, Other |
| In | Salary, Bonus, Interest/Dividends, Gift, Refund, Transfer, Other |

Categories apply to Bank accounts only. Each category has an icon. The lists are fixed in v1.

#### FR-TX-2 Balance rules
- Bank: balance = opening balance + sum of In − sum of Out in the active period. It may go below 0 (overdraft) and is then shown with a `−` sign.
- Loan: balance = opening balance − Repayments + Additional amounts.
- Investment: the value comes from holdings (§9.2.2).
- A Loan's balance may not go below 0: WHEN a repayment is larger than the remaining balance, THEN Save is disabled with "More than the remaining amount".

#### FR-TX-3 Edit and delete transactions
- WHEN the user taps a transaction in the active period, THEN the form opens with its values; saving replaces it and the balance is recalculated.
- WHEN the user deletes a transaction, THEN a confirmation is shown, and after confirming it is removed and the balance is recalculated.
- Transactions in a frozen period can be opened read-only and can't be edited or deleted.

#### FR-TX-4 Transaction timeline
Transactions are listed newest first, grouped by day with a day header ("Today", "Yesterday", "14 Nov"). Each row shows the category icon, category, note (if any), and the signed amount.

#### FR-TX-5 Read-only monthly summary
A Transactions account also shows a **Monthly** sub-view (a segmented control above the timeline: *Transactions* | *By month*) listing derived month-end balances and their changes, exactly like FR-MO-4 but read-only.

#### FR-TX-6 Spending by category
Account Detail of a Transactions-mode Bank account shows **"This month by category"**: a horizontal bar per Out category used this month, largest first, with amount and share of total Out. Hidden when there are no Out transactions this month.

#### 9.2.2 Investment accounts: trades and holdings (D14, D15)

An Investment account always records **trades** (D16) and keeps a list of **holdings**. Its value is the sum, over all holdings, of quantity held x latest price. The account holds no cash (D15): cash at the broker can be tracked as a separate account. Dividends are not recorded in v1; each trade may record a fee (D23).

##### FR-IN-1 Add a trade
Account Detail shows an **Add trade** action. It opens a form with:

| Field | Rules |
|---|---|
| Side | **Buy** / **Sell** toggle; Buy selected by default |
| Instrument | Required; free text, e.g. "VWCE" or "Bitcoin"; trimmed; suggests instruments already in this account; matched ignoring case |
| Quantity | Required; > 0; up to 8 decimals, so crypto fractions fit |
| Price per unit | Required; > 0; in the account's currency; up to 6 decimals |
| Fee | Optional; ≥ 0; in the account's currency; 2 decimals |
| Date | Today by default; no future dates; not before the account's creation date |
| Note | Optional; up to 200 characters |

The form shows the trade total (quantity x price, rounded to 2 decimals) as the user types.

- WHEN the user adds Buy, "VWCE", quantity 10, price 110.50 in a EUR account, THEN the VWCE holding is 10 at a latest price of 110.50 and the account value grows by 1,105.00.
- WHEN the user adds a Sell for more than the quantity held on that date, THEN Save is disabled with "You hold only 4 VWCE".
- WHEN a Sell brings a holding to 0, THEN the holding leaves the holdings list; its trades stay in the timeline.
- Save is enabled only when every required field is valid; otherwise a missing-fields hint is shown, as on Add Account.

##### FR-IN-2 Edit and delete trades
- Same as FR-TX-3, except that WHEN an edit or delete would make any holding negative on any date, THEN it is rejected with "This would leave a negative quantity of VWCE".

##### FR-IN-3 Latest price
- An instrument's latest price is the price from its most recent trade or price update. When both fall on the same day, the one entered last wins.

##### FR-IN-4 Update prices (J3b)
- Account Detail shows **Update prices**. It opens a list of open holdings with each latest price prefilled. The user edits any of them, and saving records a price update dated today for each changed price (one per instrument per day; the last save wins).
- WHEN the user changes VWCE from 110.50 to 112.00 and saves, THEN the VWCE value and the account value are recalculated with 112.00.
- Price updates appear in the timeline as "Price update · VWCE 112.00".

##### FR-IN-5 Holdings list
- Account Detail of a Investment account shows its holdings: instrument, quantity, latest price with its date ("Price from 3 Oct"), value, and share of the account value, largest value first, with the total underneath.
- WHEN a latest price is older than 31 days, THEN its date is shown in a warning color, so the user knows to update it.
- WHEN there are no holdings, THEN the block shows "No holdings yet" and an **Add trade** button.

##### FR-IN-6 Starting holdings
- A new Investment account starts with no holdings and a value of 0.00 (FR-AC-2). Positions bought before the account was created are entered as Buy trades dated on the creation date (D17), at the price paid or at today's price.
- Investment accounts have no mode switch, so there is no switch-day starting point to handle (D16).

##### FR-IN-7 Fees (D23)
- WHEN a trade has a fee, THEN its timeline row shows it, e.g. "Buy 10 VWCE @ 110.50 · fee 2.00".
- The fee doesn't change any holding's value or the account value.
- The holdings block shows **Fees paid** for the current year and in total, hidden when no trade has a fee.

#### 9.2.3 Loan accounts: repayments (D13)

- A Loan transaction is either a **Repayment** (lowers the balance) or an **Additional amount** (raises it), with amount (> 0), date (same rules as FR-TX-1) and an optional note. Loans have no categories.
- The balance never goes below 0 (FR-TX-2). Mark as repaid (FR-AD-9) adds a Repayment.

### 9.3 Switching tracking mode (J4)

#### FR-SW-1 The switch
For Bank and Loan accounts, Account Detail shows the account's tracking mode as a two-option control (**Monthly** | **Transactions**). Choosing the other option asks for confirmation:

> Switch "Salary" to Transactions?
> Your past entries will be kept but can't be edited. Tracking starts today from 12,340.00 MDL.
> [Switch] [Cancel]

- WHEN the user confirms, THEN the active period ends today and becomes a frozen period, and a new active period in the other mode starts today with the current balance as its starting point (D2).
- WHEN the user cancels, THEN nothing changes.

#### FR-SW-2 Monthly → Transactions
- WHEN a Monthly account with November 2026: 12,340.00 is switched on 16 Nov 2026, THEN a Transactions period starts with opening balance 12,340.00 on 16 Nov 2026, and all monthly entries become read-only.

#### FR-SW-3 Transactions → Monthly
- WHEN a Transactions account with balance 12,340.00 is switched on 16 Nov 2026, THEN a Monthly period starts with the entry November 2026: 12,340.00, which stays editable, and all transactions become read-only.

#### FR-SW-4 Timeline across periods
- The timeline shows the active period first, then each frozen period under a divider, e.g. "Monthly until 16 Nov 2026 · read-only".
- Charts and month-end balances run continuously across periods.

#### FR-SW-5 Switching more than once
- WHEN an account is switched again later, THEN another frozen period is added; there is no limit.
- WHEN an account is switched twice on the same day, THEN the period that lasted less than a day is discarded, leaving the account as if only the last switch happened (D18).

### 9.4 Month-end balance (used by charts and the Dashboard)

For each account and each month from its first month to the current month:

- **Monthly period:** the month's entry, or the carried-forward previous entry if the month is missing.
- **Transactions period:** opening balance plus all transactions dated on or before the last day of the month.
- **Investment, Transactions period:** for each instrument, the quantity held at the end of the month x its latest price on or before the last day of the month.
- **Current month:** the current balance ("so far this month").
- **Month containing a switch:** the value from the period that was active on the last day of the month (for the current month, the active period).
- Months before an account's first month don't count it (it adds 0 to totals, and its own chart starts at its first month).

---

## 10. Account Detail (J5)

### 10.1 Layout

```
+--------------------------------------------------+
| <  Salary                              [Edit][:] |
|    [MAIB] Savings - **** 0421                    |
|    12,340.00 MDL          (~ 615.20 EUR)         |
|    This month  +1,530.00 (+14.2%)                |
|                                                  |
|    [ balance chart:  6M | 1Y | All ]             |
|                                                  |
|    Tracking:  ( Monthly | Transactions )         |
|                                                  |
|    [ type-specific block ]                       |
|                                                  |
|    [ Check in ]  or  [ Add transaction ]         |
|    timeline (active period, then frozen periods) |
+--------------------------------------------------+
```

On wide windows the header, chart and type block sit in one pane and the timeline in a second pane beside it, matching the My Finances two-pane pattern.

### FR-AD-1 Header
- Shows name, bank badge or type icon, details summary (as on the card), balance in the account's currency, the Main Currency equivalent when the currencies differ, and **This month** net change (abs and %).
- WHEN the account has no month-end balance for the previous month, THEN "This month" shows "—".

### FR-AD-2 Balance chart
- A line chart of month-end balances (§9.4), with range options **6M**, **1Y**, **All**; 6M is the default.
- WHEN the account has only one month of data, THEN a single point is shown with the note "The chart fills in as months pass".
- Tapping or hovering a point shows the month and balance.

### FR-AD-3 Overflow menu
Contains **Delete account** (§8.4).

### 10.2 Type-specific blocks (D10)

#### FR-AD-4 Bank, kind Deposit: deposit info
- Shows interest rate ("7.50% per year") and maturity date ("Matures 30 Jun 2027 · in 226 days").
- WHEN the maturity date has passed, THEN it reads "Matured on 30 Jun 2027".
- WHEN neither field is filled, THEN the block is hidden.

#### FR-AD-5 Bank, any kind: In vs Out
- Transactions mode only. A paired bar chart of total In and total Out for each of the last 6 months, plus this month's totals as numbers.
- In Monthly mode this block isn't shown (the monthly timeline already shows the changes).

#### FR-AD-6 Investment: total change since start
- Shows "Since <first month>: +2,300.00 (+23.0%)", computed as the current value minus the first recorded month-end value.
- The % is hidden when the first balance is 0.

#### FR-AD-7 Loan: original amount and progress
- WHEN an original amount is set, THEN the block shows "Repaid 2,000.00 of 5,000.00" and a progress bar at 40%. Repaid = original amount − current balance, never below 0.
- WHEN no original amount is set, THEN only the remaining balance is shown, with an "Add original amount" link that opens Edit.
- The label follows the direction: Borrowed reads "You owe <counterparty>"; Lent reads "<counterparty> owes you".

#### FR-AD-8 Loan: due date
- Shows "Due 1 Mar 2027 · in 105 days".
- WHEN the due date has passed and the balance is > 0, THEN it reads "Overdue since 1 Mar 2027" in the error color, and the My Finances card shows an "Overdue" badge.
- WHEN no due date is set, THEN this line is hidden.

### 10.3 Mark loan as repaid (J10)

#### FR-AD-9 Mark as repaid
- A Loan with balance > 0 shows **Mark as repaid**. WHEN tapped, THEN a confirmation reads "Mark "<name>" as repaid? Its balance will be set to 0."
- WHEN confirmed on a Transactions-mode loan, THEN a repayment transaction for the full remaining balance, dated today, is added.
- WHEN confirmed on a Monthly-mode loan, THEN the current month's entry is set to 0.
- Afterwards the loan shows a "Repaid" badge, its card moves to the end (FR-MF-5), the action is hidden, and the loan adds 0 to net worth. It can still be edited or deleted.
- WHEN a later entry makes the balance > 0 again, THEN the Repaid badge disappears.

---

## 11. Dashboard (J7)

### 11.1 Layout

```
+--------------------------------------------------+
| Net worth                                        |
| 48,210.00 MDL                                    |
| This month +1,530.00 (+3.3%)  -  This year +6,900|
|                                                  |
| [ net worth line + monthly change bars ]         |
| [ 6M | 1Y | All ]                                |
|                                                  |
| By type                                          |
| Bank 61% ######  Investment 34% ###  Loans 5% #  |
|                                                  |
| Biggest changes this month                       |
| Brokerage   +1,500.00                            |
| Car loan    +500.00  (debt went down)            |
| Salary      -470.00                              |
+--------------------------------------------------+
```

### FR-DB-1 Net worth
- Net worth = Σ Bank + Σ Investment + Σ Lent loans − Σ Borrowed loans, each converted to the Main Currency at the latest rate.
- WHEN the user has Bank 30,000.00 MDL, Investment 1,000.00 EUR at 19.40, and a Borrowed loan 2,000.00 MDL, THEN net worth is 30,000 + 19,400 − 2,000 = 47,400.00 MDL.
- Net worth may be negative and is then shown with a `−` sign.

### FR-DB-2 Monthly and yearly change (D4)
- **This month** = this month's net worth (current) − last month's month-end net worth, abs and %.
- **This year** = current net worth − net worth at the end of last December.
- Past months are converted at **that month's month-end rate** (§12.2), so changes include exchange rate effects; this is intended (D4).
- WHEN there is no data for the comparison month, THEN the value shows "—".

### FR-DB-3 Net worth chart
- A line of month-end net worth plus bars for each month's net change (success color up, error color down), with ranges 6M (default), 1Y, All.
- Tapping or hovering a month shows its net worth and net change.

### FR-DB-4 Allocation by type
- Shows the share of total **assets** (Bank, Investment, Lent loans) by type, with amounts in the Main Currency.
- Borrowed loans are shown separately as "Debts: 2,000.00 MDL", not as a share.

### FR-DB-5 Biggest changes this month
- Lists up to 5 accounts with the largest absolute net change this month, in the Main Currency, largest first.
- For Borrowed loans the change is shown from the net worth point of view: debt going down is positive.
- Accounts with no change are left out. WHEN no account changed, THEN the section is hidden.

### FR-DB-6 Accounts without a rate
- WHEN an account's currency has no rate available, THEN it is left out of all Dashboard totals, and a notice reads "1 account isn't included: no exchange rate for USD yet." Tapping it opens Exchange rates.

### FR-DB-7 Empty state
- WHEN the user has no accounts, THEN the Dashboard shows "Add an account to see your net worth" with a button that opens Add Account.
- WHEN the user has accounts but only for the current month, THEN net worth is shown, changes show "—", and the chart shows a single point with "Your history builds up month by month".

### FR-DB-8 Navigation
- Tapping an account in "Biggest changes" opens its Account Detail.

---

## 12. Currency and exchange rates

### 12.1 Main Currency (D5, J8)

#### FR-CU-1 Choose the Main Currency
- Settings shows **Main Currency** with the current value. Tapping it offers MDL, USD, EUR, GBP and RON.
- WHEN the user selects USD, THEN the Dashboard, My Finances total and every "≈" equivalent are shown in USD right away. No stored amounts change.
- Default: MDL, or the currency chosen in onboarding.

### 12.2 Exchange rates (D6, J9)

#### FR-CU-2 Rate source
- The app downloads the official BNM rates for USD, EUR, GBP and RON against MDL.
- Any other pair is computed through MDL: EUR→USD = (EUR→MDL) ÷ (USD→MDL).

#### FR-CU-3 When rates are fetched
- On app start and when returning to the app, if the stored rates are older than today and the device is online.
- When the user taps **Refresh** in Exchange rates.
- Fetching never blocks the UI; screens use stored rates until new ones arrive, then update.

#### FR-CU-4 Which rate is used
| Value | Rate used |
|---|---|
| Current balances, net worth now, card equivalents | The latest stored rate |
| A past month (charts, monthly and yearly changes) | The rate for that month's last day (or the last BNM rate published before it) |

- WHEN an account has entries in months that have no stored rate, THEN the app fetches those month-end rates once, the next time it is online.

#### FR-CU-5 Offline and failure
- WHEN the device is offline, THEN stored rates are used and Exchange rates shows "Rates from 14 Nov 2026".
- WHEN a fetch fails, THEN stored rates stay in use, and a manual Refresh shows "Couldn't update rates. Showing rates from 14 Nov 2026."
- WHEN no rate has ever been stored for a needed currency, THEN that value shows "Rate unavailable" (FR-MF-1, FR-DB-6). Values are never shown as 0 for lack of a rate.

#### FR-CU-6 Exchange rates screen
- Read-only list: each currency's rate to MDL, e.g. "1 EUR = 19.40 MDL", plus "Updated 16 Nov 2026, 09:12" and a **Refresh** button.
- Shows the source: "Official rates of the National Bank of Moldova".

---

## 13. Settings (J8, J9)

| Item | v1 behavior |
|---|---|
| Main Currency | §12.1 |
| Exchange rates | §12.2 |
| About | App version, a short privacy note ("Your data stays on this device"), open-source licenses |

Phase 2 adds Backup & restore, Export CSV, Month-end reminder, App lock, and Erase all data.

---

## 14. Non-functional requirements

| # | Requirement |
|---|---|
| NFR-1 Offline-first | Every feature except fetching rates works without a network connection. |
| NFR-2 Local data | All data is stored on the device in the app's local database. The app itself sends no user data anywhere; its only network traffic is downloading BNM rates. The platform's automatic device backup may copy the data to the user's own Google or iCloud account (D21). |
| NFR-3 Platforms | Android, iOS and desktop have the same features. Layouts adapt to phone, tablet, foldable and desktop windows, following `docs/system/adaptive-ui.md`. |
| NFR-4 Precision | Amounts are stored in minor units; investment quantities (8 decimals) and unit prices (6 decimals) are stored as scaled whole numbers. Conversions are computed at full precision and rounded to 2 decimals only for display. |
| NFR-5 Performance | Dashboard and Account Detail show content within 500 ms for a user with 20 accounts and 5 years of history (about 5,000 transactions). |
| NFR-6 Accessibility | All actions are reachable with a screen reader; amounts and changes have spoken labels that include the sign and currency; charts have a text summary; touch targets are at least 48 dp; color is never the only signal. |
| NFR-7 Theming | Material 3 Expressive teal theme on Android/desktop and the matching semantic colors on iOS, in light, dark and high contrast. |
| NFR-8 Failures | Every storage read or write can fail; screens show a clear message and keep user input, following `docs/system/errors.md`. |
| NFR-9 Localization | v1 is in English only (D20); all user-facing text lives in resources so Romanian and Russian can be added later. |

---

## 15. Data model overview (conceptual)

```
Account
  id, name, type (Bank|Investment|Loan), currency, createdAt
  details: BankDetails(bank, kind, last4?, interestRate?, maturityDate?)
         | InvestmentDetails(kind, platform)
         | LoanDetails(direction, counterparty, originalAmount?, dueDate?)
  periods: [Period]            (exactly one active)

Period
  id, accountId, mode (Monthly|Transactions), startDate, endDate? (null = active)
  startingBalance

MonthlyEntry                   (Monthly periods)
  periodId, month (YYYY-MM), amountMinor        unique per (periodId, month)

Transaction                    (Transactions periods of Bank and Loan accounts)
  id, periodId, amountMinor (> 0), direction (In|Out), category? (Bank only), date, note?
  (Loan: Repayment = Out, Additional amount = In)

Trade                          (Transactions periods of Investment accounts)
  id, periodId, side (Buy|Sell), instrument, quantity (8 decimals), unitPrice (6 decimals), date, note?

PriceUpdate                    (Transactions periods of Investment accounts)
  periodId, instrument, date, unitPrice          unique per (periodId, instrument, date)

ExchangeRate
  currency, date, rateToMdl

Settings
  mainCurrency, onboardingCompleted
```

No existing data is migrated (D24). The app is unreleased, so the new schema replaces the one from `persist-accounts-and-card-swiper` and accounts saved by development builds are discarded.

---

## 16. Suggested delivery slices

Each slice is a separate OpenSpec change, delivered in order.

| Slice | Content | Depends on |
|---|---|---|
| S0 | Finish `persist-accounts-and-card-swiper` | — |
| S1 | Tracking mode on Add Account, periods, Monthly check-in, Account Detail (header, chart, monthly timeline), edit and delete account | S0 |
| S2 | Transactions mode: add/edit/delete transactions, categories, transaction timeline, read-only monthly summary, spending by category | S1 |
| S2b | Investment trades, holdings and price updates; loan repayments | S2 |
| S3 | Mode switching with frozen periods (Bank and Loan only) | S1, S2 |
| S4 | BNM rates, Main Currency setting, equivalents on cards | S0 |
| S5 | Dashboard | S1, S4 (S2 optional) |
| S6 | Type-specific blocks and new optional detail fields, Mark as repaid | S1 (S2 for In vs Out) |
| S7 | Guided onboarding; rename Profile to Settings | S4 (for Main Currency step) |

---

## 17. Risks and assumptions

| # | Risk / assumption | Mitigation |
|---|---|---|
| R1 | **Data loss**: no backup in v1 (D11); a lost, reset or replaced device loses all data. | Accepted for v1. The platform's automatic backup stays on (D21), which covers Android and iOS when the user has it enabled; desktop has no equivalent. Backup is the first Phase 2 item. |
| R2 | BNM may change its rate format or be unreachable. | Rates are cached; failures fall back to stored rates; the parser is isolated so it can be fixed quickly. |
| R3 | With net change only (D4), deposits look like gains, and exchange rate moves show as gains or losses. | The Dashboard labels it "change", not "profit". Contributions/performance is in Phase 2. |
| R4 | Users may not understand frozen periods after switching modes. | Clear confirmation text, a visible divider with the switch date, read-only styling. |
| R5 | Carry-forward makes a forgotten check-in look like "no change". | "Not checked in" label on missing months. The Phase 2 reminder addresses the root cause. |
| A1 | Users hold at most a few dozen accounts. | Card swiper and Dashboard lists are sized for this. |
| A2 | Only the five listed currencies are needed in v1. | The currency list is data, so more can be added later. |
| R6 | Investment values go stale if the user doesn't update prices (D14). | Price dates are shown on every holding and highlighted after 31 days. Live prices are in Phase 2. |

---

## 18. Questions

### 18.1 Resolved

| # | Question | Answer |
|---|---|---|
| Q1 | Rename the Profile tab to Settings? | Yes (D12). |
| Q2 | What is a Loan transaction? What is an Investment transaction? | Loans: Repayment / Additional amount (D13). Investments: trades with holdings valued at manually entered prices (D14), holdings only (D15), always tracked by trades (D16). |
| Q3 | How far back can monthly entries go? | Only back to the month the account was created (D17). |
| Q4 | Two mode switches on the same day? | The short period is discarded (D18). |
| Q5 | Combined total on My Finances? | No, Dashboard only (D19). |
| Q6 | Languages in v1? | English only (D20). |
| Q7 | Keep the platform's automatic backup? | Yes (D21). |
| Q8 | Which tab opens at launch? | Dashboard, or My Finances when there are no accounts (D22). |
| Q9 | Investment accounts and tracking modes? | Investments are always tracked by trades; no Monthly mode, no switch (D16). |
| Q10 | Fees on trades? | Optional fee field, informational only (D23). |
| Q11 | Past history before account creation? | Not in v1; history builds from the creation month (D17). |
| Q12 | Migrating accounts saved by today's app? | No migration; fresh database (D24). |

### 18.2 Open

None at the moment.
