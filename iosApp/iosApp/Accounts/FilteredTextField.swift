import SwiftUI

/// A `TextField` whose model may rewrite what was typed (e.g. "," -> ".", a third decimal dropped).
///
/// A plain `TextField(text:)` keeps showing the typed text when the binding's setter stores a
/// different value. This view mirrors the model value in local state and, after every edit, reads
/// the model's value back through `value`, so the screen always shows what the model holds. The
/// model stays the source of truth; the local copy only exists for display.
struct FilteredTextField: View {
    let placeholder: String
    var prompt: Text?
    /// Getter reads the model; setter sends the typed text to the model, which filters it.
    let value: Binding<String>

    @State private var text = ""

    var body: some View {
        TextField(placeholder, text: $text, prompt: prompt)
            .onAppear { text = value.wrappedValue }
            .onChange(of: text) { _, typed in
                guard typed != value.wrappedValue else { return }
                value.wrappedValue = typed
                let stored = value.wrappedValue
                if stored != typed { text = stored }
            }
            .onChange(of: value.wrappedValue) { _, stored in
                if text != stored { text = stored }
            }
    }
}
