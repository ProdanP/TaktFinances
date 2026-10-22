# Errors

A call the screen can recover from returns `Outcome<T>` from `:core`. The failure is a value. Do not throw it, and do not use `kotlin.Result`. That type is inline, so the success value disappears when Swift imports `SharedLogic`.

```kotlin
sealed interface Outcome<out T : Any> {
    data class Ok<T : Any>(val value: T) : Outcome<T>
    data class Failed(val reason: Reason) : Outcome<Nothing>
}

sealed interface Reason {
    data object Unavailable : Reason
}
```

`Outcome` lives in `:core` from the first repository that can fail. Every feature returns the same type.

Add a `Reason` variant only when the screen or a use case behaves differently for it. A reason that belongs to one feature is a type in that feature, passed as `reason`. It is not a second type parameter on `Outcome`.

The repository catches SQLDelight and Ktor failures and returns `Failed(Reason.Unavailable)`. Rethrow `CancellationException` before that mapping. A bug may still throw.

Android and desktop fold the value into screen state. Swift receives `Ok.value` as `Any` and casts it to the domain type. A function whose success value Swift must use without a cast returns a concrete sealed type for that one call.

Do not add Arrow or SKIE for this.
