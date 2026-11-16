package com.prodan.taktfinances.accounts.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prodan.taktfinances.accounts.domain.entity.Bank
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import taktfinances.sharedui.generated.resources.Res
import taktfinances.sharedui.generated.resources.bank_maib
import taktfinances.sharedui.generated.resources.bank_micb
import taktfinances.sharedui.generated.resources.bank_mobiasbanca
import taktfinances.sharedui.generated.resources.bank_procredit
import taktfinances.sharedui.generated.resources.bank_victoriabank

/** Bank logo in a circle; banks without a logo fall back to a monogram badge. */
@Composable
fun BankIcon(bank: Bank, modifier: Modifier = Modifier, size: Dp = 28.dp) {
    val logo = bank.logo
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(if (logo != null) Color.White else Color(bank.colorArgb)),
        contentAlignment = Alignment.Center,
    ) {
        if (logo != null) {
            Image(
                painter = painterResource(logo),
                contentDescription = bank.shortName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size),
            )
        } else Text(
            text = bank.initials,
            color = Color.White,
            fontSize = (size.value * 0.4f).sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
    }
}

private val Bank.logo: DrawableResource?
    get() = when (this) {
        Bank.Maib -> Res.drawable.bank_maib
        Bank.Victoriabank -> Res.drawable.bank_victoriabank
        Bank.Micb -> Res.drawable.bank_micb
        Bank.ProCredit -> Res.drawable.bank_procredit
        Bank.Mobiasbanca -> Res.drawable.bank_mobiasbanca
        Bank.Other -> null
    }
