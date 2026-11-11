import SwiftUI

struct FinancesColorScheme {
    let primary: Color
    let onPrimary: Color
    let primaryContainer: Color
    let onPrimaryContainer: Color
    let inversePrimary: Color
    let secondary: Color
    let onSecondary: Color
    let secondaryContainer: Color
    let onSecondaryContainer: Color
    let tertiary: Color
    let onTertiary: Color
    let tertiaryContainer: Color
    let onTertiaryContainer: Color
    let background: Color
    let onBackground: Color
    let surface: Color
    let onSurface: Color
    let surfaceVariant: Color
    let onSurfaceVariant: Color
    let surfaceTint: Color
    let inverseSurface: Color
    let inverseOnSurface: Color
    let error: Color
    let onError: Color
    let errorContainer: Color
    let onErrorContainer: Color
    let outline: Color
    let outlineVariant: Color
    let scrim: Color
    let surfaceBright: Color
    let surfaceDim: Color
    let surfaceContainer: Color
    let surfaceContainerHigh: Color
    let surfaceContainerHighest: Color
    let surfaceContainerLow: Color
    let surfaceContainerLowest: Color
    let primaryFixed: Color
    let primaryFixedDim: Color
    let onPrimaryFixed: Color
    let onPrimaryFixedVariant: Color
    let secondaryFixed: Color
    let secondaryFixedDim: Color
    let onSecondaryFixed: Color
    let onSecondaryFixedVariant: Color
    let tertiaryFixed: Color
    let tertiaryFixedDim: Color
    let onTertiaryFixed: Color
    let onTertiaryFixedVariant: Color
}

enum FinancesPalette {
    static func scheme(colorScheme: ColorScheme, contrast: ColorSchemeContrast) -> FinancesColorScheme {
        let increased = contrast == .increased
        switch colorScheme {
        case .dark:
            return increased ? darkHighContrast : dark
        default:
            return increased ? lightHighContrast : light
        }
    }

    static let light = FinancesColorScheme(
        primary: color(0x1C6873),
        onPrimary: color(0xECFCFF),
        primaryContainer: color(0xA9EEFA),
        onPrimaryContainer: color(0x015B65),
        inversePrimary: color(0xA9EEFA),
        secondary: color(0x4B6368),
        onSecondary: color(0xECFCFF),
        secondaryContainer: color(0xCDE7EC),
        onSecondaryContainer: color(0x3E565A),
        tertiary: color(0x436281),
        onTertiary: color(0xF7F9FF),
        tertiaryContainer: color(0xBADAFF),
        onTertiaryContainer: color(0x2E4D6C),
        background: color(0xF6FAFB),
        onBackground: color(0x2A3436),
        surface: color(0xF6FAFB),
        onSurface: color(0x2A3436),
        surfaceVariant: color(0xDAE4E7),
        onSurfaceVariant: color(0x576163),
        surfaceTint: color(0x1C6873),
        inverseSurface: color(0x0A0F10),
        inverseOnSurface: color(0x999E9F),
        error: color(0xA83836),
        onError: color(0xFFF7F6),
        errorContainer: color(0xFA746F),
        onErrorContainer: color(0x6E0A12),
        outline: color(0x727C7F),
        outlineVariant: color(0xA9B4B6),
        scrim: color(0x000000),
        surfaceBright: color(0xF6FAFB),
        surfaceDim: color(0xD1DCDE),
        surfaceContainer: color(0xE8EFF1),
        surfaceContainerHigh: color(0xE1EAEB),
        surfaceContainerHighest: color(0xDAE4E7),
        surfaceContainerLow: color(0xEEF5F6),
        surfaceContainerLowest: color(0xFFFFFF),
        primaryFixed: color(0xA9EEFA),
        primaryFixedDim: color(0x9BDFEC),
        onPrimaryFixed: color(0x00464F),
        onPrimaryFixedVariant: color(0x17656F),
        secondaryFixed: color(0xCDE7EC),
        secondaryFixedDim: color(0xBFD9DE),
        onSecondaryFixed: color(0x2B4347),
        onSecondaryFixedVariant: color(0x475F64),
        tertiaryFixed: color(0xBADAFF),
        tertiaryFixedDim: color(0xADCCF1),
        onTertiaryFixed: color(0x183A58),
        onTertiaryFixedVariant: color(0x375776)
    )

    static let dark = FinancesColorScheme(
        primary: color(0x9DCFD8),
        onPrimary: color(0x0E474F),
        primaryContainer: color(0x265962),
        onPrimaryContainer: color(0xB9ECF6),
        inversePrimary: color(0x34666F),
        secondary: color(0xB1CBD0),
        onSecondary: color(0x2C4448),
        secondaryContainer: color(0x273F43),
        onSecondaryContainer: color(0xAAC4C9),
        tertiary: color(0xD4E7FF),
        onTertiary: color(0x375675),
        tertiaryContainer: color(0xBADAFF),
        onTertiaryContainer: color(0x2E4D6C),
        background: color(0x0A0F10),
        onBackground: color(0xDDE7E9),
        surface: color(0x0A0F10),
        onSurface: color(0xDDE7E9),
        surfaceVariant: color(0x1E2729),
        onSurfaceVariant: color(0xA2ADAF),
        surfaceTint: color(0x9DCFD8),
        inverseSurface: color(0xF6FAFB),
        inverseOnSurface: color(0x515657),
        error: color(0xFA746F),
        onError: color(0x490006),
        errorContainer: color(0x871F21),
        onErrorContainer: color(0xFF9993),
        outline: color(0x6D7779),
        outlineVariant: color(0x404A4C),
        scrim: color(0x000000),
        surfaceBright: color(0x242E30),
        surfaceDim: color(0x0A0F10),
        surfaceContainer: color(0x131B1C),
        surfaceContainerHigh: color(0x192123),
        surfaceContainerHighest: color(0x1E2729),
        surfaceContainerLow: color(0x0E1416),
        surfaceContainerLowest: color(0x000000),
        primaryFixed: color(0xB8EBF5),
        primaryFixedDim: color(0xAADDE7),
        onPrimaryFixed: color(0x0C464E),
        onPrimaryFixedVariant: color(0x30636B),
        secondaryFixed: color(0xCDE7EC),
        secondaryFixedDim: color(0xBFD9DE),
        onSecondaryFixed: color(0x2B4347),
        onSecondaryFixedVariant: color(0x475F64),
        tertiaryFixed: color(0xBADAFF),
        tertiaryFixedDim: color(0xADCCF1),
        onTertiaryFixed: color(0x183A58),
        onTertiaryFixedVariant: color(0x375776)
    )

    static let lightHighContrast = FinancesColorScheme(
        primary: color(0x00282E),
        onPrimary: color(0xA1E5F2),
        primaryContainer: color(0x025B66),
        onPrimaryContainer: color(0xFFFFFF),
        inversePrimary: color(0xA9EEFA),
        secondary: color(0x0E272B),
        onSecondary: color(0xC5DFE4),
        secondaryContainer: color(0x3E565B),
        onSecondaryContainer: color(0xFFFFFF),
        tertiary: color(0x002641),
        onTertiary: color(0xC0DDFF),
        tertiaryContainer: color(0x355573),
        onTertiaryContainer: color(0xFFFFFF),
        background: color(0xF6FAFB),
        onBackground: color(0x000000),
        surface: color(0xF6FAFB),
        onSurface: color(0x000000),
        surfaceVariant: color(0xDAE4E7),
        onSurfaceVariant: color(0x1C2627),
        surfaceTint: color(0x00282E),
        inverseSurface: color(0x0A0F10),
        inverseOnSurface: color(0xFFFFFF),
        error: color(0x510007),
        onError: color(0xFFCFCB),
        errorContainer: color(0x972B2B),
        onErrorContainer: color(0xFFFFFF),
        outline: color(0x3B4546),
        outlineVariant: color(0x4A5456),
        scrim: color(0x000000),
        surfaceBright: color(0xF6FAFB),
        surfaceDim: color(0xD1DCDE),
        surfaceContainer: color(0xE8EFF1),
        surfaceContainerHigh: color(0xE1EAEB),
        surfaceContainerHighest: color(0xDAE4E7),
        surfaceContainerLow: color(0xEEF5F6),
        surfaceContainerLowest: color(0xFFFFFF),
        primaryFixed: color(0x025B66),
        primaryFixedDim: color(0x004E58),
        onPrimaryFixed: color(0xFFFFFF),
        onPrimaryFixedVariant: color(0xFFFFFF),
        secondaryFixed: color(0x3E565B),
        secondaryFixedDim: color(0x324A4F),
        onSecondaryFixed: color(0xFFFFFF),
        onSecondaryFixedVariant: color(0xFFFFFF),
        tertiaryFixed: color(0x355573),
        tertiaryFixedDim: color(0x294967),
        onTertiaryFixed: color(0xFFFFFF),
        onTertiaryFixedVariant: color(0xFFFFFF)
    )

    static let darkHighContrast = FinancesColorScheme(
        primary: color(0xBBEEF8),
        onPrimary: color(0x003138),
        primaryContainer: color(0x7AACB5),
        onPrimaryContainer: color(0x000000),
        inversePrimary: color(0x003F47),
        secondary: color(0xD0EAEF),
        onSecondary: color(0x173034),
        secondaryContainer: color(0x8FA8AD),
        onSecondaryContainer: color(0x000000),
        tertiary: color(0xD4E7FF),
        onTertiary: color(0x082E4B),
        tertiaryContainer: color(0xBADAFF),
        onTertiaryContainer: color(0x00233E),
        background: color(0x0A0F10),
        onBackground: color(0xFFFFFF),
        surface: color(0x0A0F10),
        onSurface: color(0xFFFFFF),
        surfaceVariant: color(0x1E2729),
        onSurfaceVariant: color(0xDDE7E9),
        surfaceTint: color(0xBBEEF8),
        inverseSurface: color(0xF6FAFB),
        inverseOnSurface: color(0x000000),
        error: color(0xFFDEDB),
        onError: color(0x60000A),
        errorContainer: color(0xFF7C76),
        onErrorContainer: color(0x000000),
        outline: color(0xB0BBBD),
        outlineVariant: color(0x9BA6A8),
        scrim: color(0x000000),
        surfaceBright: color(0x242E30),
        surfaceDim: color(0x0A0F10),
        surfaceContainer: color(0x131B1C),
        surfaceContainerHigh: color(0x192123),
        surfaceContainerHighest: color(0x1E2729),
        surfaceContainerLow: color(0x0E1416),
        surfaceContainerLowest: color(0x000000),
        primaryFixed: color(0xB8EBF5),
        primaryFixedDim: color(0xAADDE7),
        onPrimaryFixed: color(0x000000),
        onPrimaryFixedVariant: color(0x002429),
        secondaryFixed: color(0xCDE7EC),
        secondaryFixedDim: color(0xBFD9DE),
        onSecondaryFixed: color(0x000000),
        onSecondaryFixedVariant: color(0x092327),
        tertiaryFixed: color(0xBADAFF),
        tertiaryFixedDim: color(0xADCCF1),
        onTertiaryFixed: color(0x000000),
        onTertiaryFixedVariant: color(0x001629)
    )
}

private func color(_ hex: UInt32) -> Color {
    Color(
        red: Double((hex >> 16) & 0xFF) / 255,
        green: Double((hex >> 8) & 0xFF) / 255,
        blue: Double(hex & 0xFF) / 255
    )
}
