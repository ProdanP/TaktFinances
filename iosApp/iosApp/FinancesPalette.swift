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
    static func scheme(colorScheme: ColorScheme) -> FinancesColorScheme {
        switch colorScheme {
        case .dark:
            return dark
        default:
            return light
        }
    }

    static let light = FinancesColorScheme(
        primary: color(0x39608F),
        onPrimary: color(0xFFFFFF),
        primaryContainer: color(0xD3E4FF),
        onPrimaryContainer: color(0x1E4876),
        inversePrimary: color(0xA3C9FE),
        secondary: color(0x545F70),
        onSecondary: color(0xFFFFFF),
        secondaryContainer: color(0xD7E3F8),
        onSecondaryContainer: color(0x3C4758),
        tertiary: color(0x6C5677),
        onTertiary: color(0xFFFFFF),
        tertiaryContainer: color(0xF5D9FF),
        onTertiaryContainer: color(0x543F5E),
        background: color(0xF8F9FF),
        onBackground: color(0x191C20),
        surface: color(0xF8F9FF),
        onSurface: color(0x191C20),
        surfaceVariant: color(0xDFE2EB),
        onSurfaceVariant: color(0x43474E),
        surfaceTint: color(0x39608F),
        inverseSurface: color(0x2E3035),
        inverseOnSurface: color(0xEFF0F7),
        error: color(0xBA1A1A),
        onError: color(0xFFFFFF),
        errorContainer: color(0xFFDAD6),
        onErrorContainer: color(0x93000A),
        outline: color(0x73777F),
        outlineVariant: color(0xC3C6CF),
        scrim: color(0x000000),
        surfaceBright: color(0xF8F9FF),
        surfaceDim: color(0xD8DAE0),
        surfaceContainer: color(0xECEDF4),
        surfaceContainerHigh: color(0xE7E8EE),
        surfaceContainerHighest: color(0xE1E2E8),
        surfaceContainerLow: color(0xF2F3FA),
        surfaceContainerLowest: color(0xFFFFFF),
        primaryFixed: color(0xD3E4FF),
        primaryFixedDim: color(0xA3C9FE),
        onPrimaryFixed: color(0x001C38),
        onPrimaryFixedVariant: color(0x1E4876),
        secondaryFixed: color(0xD7E3F8),
        secondaryFixedDim: color(0xBBC7DB),
        onSecondaryFixed: color(0x101C2B),
        onSecondaryFixedVariant: color(0x3C4758),
        tertiaryFixed: color(0xF5D9FF),
        tertiaryFixedDim: color(0xD8BDE3),
        onTertiaryFixed: color(0x261431),
        onTertiaryFixedVariant: color(0x543F5E)
    )

    static let dark = FinancesColorScheme(
        primary: color(0xA3C9FE),
        onPrimary: color(0x00315B),
        primaryContainer: color(0x1E4876),
        onPrimaryContainer: color(0xD3E4FF),
        inversePrimary: color(0x39608F),
        secondary: color(0xBBC7DB),
        onSecondary: color(0x263141),
        secondaryContainer: color(0x3C4758),
        onSecondaryContainer: color(0xD7E3F8),
        tertiary: color(0xD8BDE3),
        onTertiary: color(0x3C2947),
        tertiaryContainer: color(0x543F5E),
        onTertiaryContainer: color(0xF5D9FF),
        background: color(0x111418),
        onBackground: color(0xE1E2E8),
        surface: color(0x111418),
        onSurface: color(0xE1E2E8),
        surfaceVariant: color(0x43474E),
        onSurfaceVariant: color(0xC3C6CF),
        surfaceTint: color(0xA3C9FE),
        inverseSurface: color(0xE1E2E8),
        inverseOnSurface: color(0x2E3035),
        error: color(0xFFB4AB),
        onError: color(0x690005),
        errorContainer: color(0x93000A),
        onErrorContainer: color(0xFFDAD6),
        outline: color(0x8D9199),
        outlineVariant: color(0x43474E),
        scrim: color(0x000000),
        surfaceBright: color(0x37393E),
        surfaceDim: color(0x111418),
        surfaceContainer: color(0x1D2024),
        surfaceContainerHigh: color(0x272A2F),
        surfaceContainerHighest: color(0x32353A),
        surfaceContainerLow: color(0x191C20),
        surfaceContainerLowest: color(0x0C0E13),
        primaryFixed: color(0xD3E4FF),
        primaryFixedDim: color(0xA3C9FE),
        onPrimaryFixed: color(0x001C38),
        onPrimaryFixedVariant: color(0x1E4876),
        secondaryFixed: color(0xD7E3F8),
        secondaryFixedDim: color(0xBBC7DB),
        onSecondaryFixed: color(0x101C2B),
        onSecondaryFixedVariant: color(0x3C4758),
        tertiaryFixed: color(0xF5D9FF),
        tertiaryFixedDim: color(0xD8BDE3),
        onTertiaryFixed: color(0x261431),
        onTertiaryFixedVariant: color(0x543F5E)
    )
}

private func color(_ hex: UInt32) -> Color {
    Color(
        red: Double((hex >> 16) & 0xFF) / 255,
        green: Double((hex >> 8) & 0xFF) / 255,
        blue: Double(hex & 0xFF) / 255
    )
}
