package arrow.intro.algebras
import arrow.Kind
import arrow.effects.typeclasses.MonadDefer

interface Logger<F> {
  fun log(msg: String) : Kind<F, Unit>
}

class DefaultConsoleLogger<F>(private val delay: MonadDefer<F>): Logger<F>, MonadDefer<F> by delay {
  override fun log(msg: String): Kind<F, Unit> =
    delay { println(msg) }
}