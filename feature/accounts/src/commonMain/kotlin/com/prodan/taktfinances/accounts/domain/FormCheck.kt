package com.prodan.taktfinances.accounts.domain

/** Fields of the Add Account form, in the order the form presents them. */
enum class FormField(internal val verb: HintVerb, internal val noun: String, internal val article: String) {
    Name(HintVerb.Enter, "name", "a"),
    Amount(HintVerb.Enter, "amount", "an"),
    Bank(HintVerb.Choose, "bank", "a"),
    AccountKind(HintVerb.Choose, "kind", "a"),
    Last4(HintVerb.Enter, "last 4 digits", "the"),
    InvestmentKind(HintVerb.Choose, "kind", "a"),
    Platform(HintVerb.Enter, "platform", "a"),
    Direction(HintVerb.Choose, "direction", "a"),
    Counterparty(HintVerb.Enter, "counterparty", "a"),
}

internal enum class HintVerb(val word: String) { Enter("enter"), Choose("choose") }

/** Result of checking the form: which fields of the selected type are missing or invalid. */
data class FormCheck(val invalid: Set<FormField>) {
    val canSave: Boolean get() = invalid.isEmpty()

    /** [invalid] in form order. */
    val missing: List<FormField> get() = FormField.entries.filter { it in invalid }

    /** Sentence naming what is still missing, or null when the form can be saved. */
    val hint: String? get() = missingFieldsHint(missing)
}

/** Checks the fields that belong to [type]; details of other types are ignored. */
fun checkAccountForm(
    type: AccountType,
    name: String,
    amount: String,
    details: AccountDetailsDraft,
): FormCheck {
    val invalid = buildSet {
        if (name.isBlank()) add(FormField.Name)
        if (parseAmountMinor(amount) == null) add(FormField.Amount)
        when (type) {
            AccountType.BankAccount -> {
                if (details.bank == null) add(FormField.Bank)
                if (details.bankAccountKind == null) add(FormField.AccountKind)
                val last4 = details.last4.trim()
                if (last4.isNotEmpty() && !LAST4.matches(last4)) add(FormField.Last4)
            }
            AccountType.Investment -> {
                if (details.investmentKind == null) add(FormField.InvestmentKind)
                if (details.platform.isBlank()) add(FormField.Platform)
            }
            AccountType.Loan -> {
                if (details.loanDirection == null) add(FormField.Direction)
                if (details.counterparty.isBlank()) add(FormField.Counterparty)
            }
        }
    }
    return FormCheck(invalid)
}

/**
 * Builds a short sentence from [fields] (in form order), grouping neighbours that share a verb:
 * "Enter a name and amount, choose a bank and kind". Returns null for an empty list.
 */
fun missingFieldsHint(fields: List<FormField>): String? {
    if (fields.isEmpty()) return null
    val runs = mutableListOf<MutableList<FormField>>()
    for (field in fields) {
        val last = runs.lastOrNull()
        if (last != null && last.first().verb == field.verb) last += field else runs += mutableListOf(field)
    }
    val sentence = runs.joinToString(", ") { run ->
        val nouns = run.map { it.noun }.distinct()
        "${run.first().verb.word} ${run.first().article} ${joinNouns(nouns)}"
    }
    return sentence.replaceFirstChar { it.uppercaseChar() }
}

private fun joinNouns(nouns: List<String>): String = when (nouns.size) {
    1 -> nouns.single()
    else -> nouns.dropLast(1).joinToString(", ") + " and " + nouns.last()
}

internal val LAST4 = Regex("""\d{4}""")
