import SwiftUI

struct LandingView: View {
    var onContinue: () -> Void = {}

    @Environment(\.colorScheme) private var colorScheme
    @Environment(\.colorSchemeContrast) private var colorSchemeContrast
    @State private var model: LandingModel = LandingModel()

    var body: some View {
        let palette = FinancesPalette.scheme(
            colorScheme: colorScheme,
            contrast: colorSchemeContrast
        )
        VStack(spacing: 24) {
            Text(model.state.title)
                .font(.title)
                .foregroundStyle(palette.onBackground)
                .multilineTextAlignment(.center)
                .frame(maxWidth: .infinity)

            Spacer(minLength: 0)

            VStack(spacing: 24) {
                Image("Landing")
                    .resizable()
                    .scaledToFit()
                    .frame(width: 240, height: 240)

                Text(model.state.message)
                    .font(.body)
                    .foregroundStyle(palette.onSurfaceVariant)
                    .multilineTextAlignment(.center)
                    .frame(maxWidth: .infinity)
            }

            Spacer(minLength: 0)

            Button {
                model.send(.continueTapped)
                onContinue()
            } label: {
                Text("Continue")
                    .font(.body.weight(.medium))
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 14)
            }
            .buttonStyle(.plain)
            .foregroundStyle(palette.onPrimary)
            .background(palette.primary, in: RoundedRectangle(cornerRadius: 12, style: .continuous))
        }
        .padding(.horizontal, 24)
        .padding(.top, 48)
        .padding(.bottom, 24)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(palette.background)
    }
}

struct LandingView_Previews: PreviewProvider {
    static var previews: some View {
        LandingView()
    }
}
