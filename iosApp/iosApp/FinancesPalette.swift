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
        primary: color(0x415F91),
        onPrimary: color(0xFFFFFF),
        primaryContainer: color(0xD6E3FF),
        onPrimaryContainer: color(0x284777),
        inversePrimary: color(0xAAC7FF),
        secondary: color(0x565F71),
        onSecondary: color(0xFFFFFF),
        secondaryContainer: color(0xDAE2F9),
        onSecondaryContainer: color(0x3E4759),
        tertiary: color(0x705575),
        onTertiary: color(0xFFFFFF),
        tertiaryContainer: color(0xFAD8FD),
        onTertiaryContainer: color(0x573E5C),
        background: color(0xF9F9FF),
        onBackground: color(0x191C20),
        surface: color(0xF9F9FF),
        onSurface: color(0x191C20),
        surfaceVariant: color(0xE0E2EC),
        onSurfaceVariant: color(0x44474E),
        surfaceTint: color(0x415F91),
        inverseSurface: color(0x2E3036),
        inverseOnSurface: color(0xF0F0F7),
        error: color(0xBA1A1A),
        onError: color(0xFFFFFF),
        errorContainer: color(0xFFDAD6),
        onErrorContainer: color(0x93000A),
        outline: color(0x74777F),
        outlineVariant: color(0xC4C6D0),
        scrim: color(0x000000),
        surfaceBright: color(0xF9F9FF),
        surfaceDim: color(0xD9D9E0),
        surfaceContainer: color(0xEDEDF4),
        surfaceContainerHigh: color(0xE7E8EE),
        surfaceContainerHighest: color(0xE2E2E9),
        surfaceContainerLow: color(0xF3F3FA),
        surfaceContainerLowest: color(0xFFFFFF),
        primaryFixed: color(0xD6E3FF),
        primaryFixedDim: color(0xAAC7FF),
        onPrimaryFixed: color(0x001B3E),
        onPrimaryFixedVariant: color(0x284777),
        secondaryFixed: color(0xDAE2F9),
        secondaryFixedDim: color(0xBEC6DC),
        onSecondaryFixed: color(0x131C2B),
        onSecondaryFixedVariant: color(0x3E4759),
        tertiaryFixed: color(0xFAD8FD),
        tertiaryFixedDim: color(0xDDBCE0),
        onTertiaryFixed: color(0x28132E),
        onTertiaryFixedVariant: color(0x573E5C)
    )

    static let dark = FinancesColorScheme(
        primary: color(0xAAC7FF),
        onPrimary: color(0x0A305F),
        primaryContainer: color(0x284777),
        onPrimaryContainer: color(0xD6E3FF),
        inversePrimary: color(0x415F91),
        secondary: color(0xBEC6DC),
        onSecondary: color(0x283141),
        secondaryContainer: color(0x3E4759),
        onSecondaryContainer: color(0xDAE2F9),
        tertiary: color(0xDDBCE0),
        onTertiary: color(0x3F2844),
        tertiaryContainer: color(0x573E5C),
        onTertiaryContainer: color(0xFAD8FD),
        background: color(0x111318),
        onBackground: color(0xE2E2E9),
        surface: color(0x111318),
        onSurface: color(0xE2E2E9),
        surfaceVariant: color(0x44474E),
        onSurfaceVariant: color(0xC4C6D0),
        surfaceTint: color(0xAAC7FF),
        inverseSurface: color(0xE2E2E9),
        inverseOnSurface: color(0x2E3036),
        error: color(0xFFB4AB),
        onError: color(0x690005),
        errorContainer: color(0x93000A),
        onErrorContainer: color(0xFFDAD6),
        outline: color(0x8E9099),
        outlineVariant: color(0x44474E),
        scrim: color(0x000000),
        surfaceBright: color(0x37393E),
        surfaceDim: color(0x111318),
        surfaceContainer: color(0x1D2024),
        surfaceContainerHigh: color(0x282A2F),
        surfaceContainerHighest: color(0x33353A),
        surfaceContainerLow: color(0x191C20),
        surfaceContainerLowest: color(0x0C0E13),
        primaryFixed: color(0xD6E3FF),
        primaryFixedDim: color(0xAAC7FF),
        onPrimaryFixed: color(0x001B3E),
        onPrimaryFixedVariant: color(0x284777),
        secondaryFixed: color(0xDAE2F9),
        secondaryFixedDim: color(0xBEC6DC),
        onSecondaryFixed: color(0x131C2B),
        onSecondaryFixedVariant: color(0x3E4759),
        tertiaryFixed: color(0xFAD8FD),
        tertiaryFixedDim: color(0xDDBCE0),
        onTertiaryFixed: color(0x28132E),
        onTertiaryFixedVariant: color(0x573E5C)
    )

    static let lightHighContrast = FinancesColorScheme(
        primary: color(0x032B5B),
        onPrimary: color(0xFFFFFF),
        primaryContainer: color(0x2A497A),
        onPrimaryContainer: color(0xFFFFFF),
        inversePrimary: color(0xAAC7FF),
        secondary: color(0x232C3D),
        onSecondary: color(0xFFFFFF),
        secondaryContainer: color(0x41495B),
        onSecondaryContainer: color(0xFFFFFF),
        tertiary: color(0x3A2440),
        onTertiary: color(0xFFFFFF),
        tertiaryContainer: color(0x59405E),
        onTertiaryContainer: color(0xFFFFFF),
        background: color(0xF9F9FF),
        onBackground: color(0x191C20),
        surface: color(0xF9F9FF),
        onSurface: color(0x000000),
        surfaceVariant: color(0xE0E2EC),
        onSurfaceVariant: color(0x000000),
        surfaceTint: color(0x415F91),
        inverseSurface: color(0x2E3036),
        inverseOnSurface: color(0xFFFFFF),
        error: color(0x600004),
        onError: color(0xFFFFFF),
        errorContainer: color(0x98000A),
        onErrorContainer: color(0xFFFFFF),
        outline: color(0x292C33),
        outlineVariant: color(0x464951),
        scrim: color(0x000000),
        surfaceBright: color(0xF9F9FF),
        surfaceDim: color(0xB8B8BF),
        surfaceContainer: color(0xE2E2E9),
        surfaceContainerHigh: color(0xD3D4DB),
        surfaceContainerHighest: color(0xC5C6CD),
        surfaceContainerLow: color(0xF0F0F7),
        surfaceContainerLowest: color(0xFFFFFF),
        primaryFixed: color(0x2A497A),
        primaryFixedDim: color(0x0E3262),
        onPrimaryFixed: color(0xFFFFFF),
        onPrimaryFixedVariant: color(0xFFFFFF),
        secondaryFixed: color(0x41495B),
        secondaryFixedDim: color(0x2A3344),
        onSecondaryFixed: color(0xFFFFFF),
        onSecondaryFixedVariant: color(0xFFFFFF),
        tertiaryFixed: color(0x59405E),
        tertiaryFixedDim: color(0x412A47),
        onTertiaryFixed: color(0xFFFFFF),
        onTertiaryFixedVariant: color(0xFFFFFF)
    )

    static let darkHighContrast = FinancesColorScheme(
        primary: color(0xEBF0FF),
        onPrimary: color(0x000000),
        primaryContainer: color(0xA6C3FC),
        onPrimaryContainer: color(0x000B20),
        inversePrimary: color(0x294878),
        secondary: color(0xEBF0FF),
        onSecondary: color(0x000000),
        secondaryContainer: color(0xBAC3D8),
        onSecondaryContainer: color(0x030B1A),
        tertiary: color(0xFFE9FF),
        onTertiary: color(0x000000),
        tertiaryContainer: color(0xD8B8DC),
        onTertiaryContainer: color(0x16041D),
        background: color(0x111318),
        onBackground: color(0xE2E2E9),
        surface: color(0x111318),
        onSurface: color(0xFFFFFF),
        surfaceVariant: color(0x44474E),
        onSurfaceVariant: color(0xFFFFFF),
        surfaceTint: color(0xAAC7FF),
        inverseSurface: color(0xE2E2E9),
        inverseOnSurface: color(0x000000),
        error: color(0xFFECE9),
        onError: color(0x000000),
        errorContainer: color(0xFFAEA4),
        onErrorContainer: color(0x220001),
        outline: color(0xEEEFF9),
        outlineVariant: color(0xC0C2CC),
        scrim: color(0x000000),
        surfaceBright: color(0x4E5056),
        surfaceDim: color(0x111318),
        surfaceContainer: color(0x2E3036),
        surfaceContainerHigh: color(0x393B41),
        surfaceContainerHighest: color(0x45474C),
        surfaceContainerLow: color(0x1D2024),
        surfaceContainerLowest: color(0x000000),
        primaryFixed: color(0xD6E3FF),
        primaryFixedDim: color(0xAAC7FF),
        onPrimaryFixed: color(0x000000),
        onPrimaryFixedVariant: color(0x00112B),
        secondaryFixed: color(0xDAE2F9),
        secondaryFixedDim: color(0xBEC6DC),
        onSecondaryFixed: color(0x000000),
        onSecondaryFixedVariant: color(0x081121),
        tertiaryFixed: color(0xFAD8FD),
        tertiaryFixedDim: color(0xDDBCE0),
        onTertiaryFixed: color(0x000000),
        onTertiaryFixedVariant: color(0x1D0823)
    )
}

private func color(_ hex: UInt32) -> Color {
    Color(
        red: Double((hex >> 16) & 0xFF) / 255,
        green: Double((hex >> 8) & 0xFF) / 255,
        blue: Double(hex & 0xFF) / 255
    )
}
