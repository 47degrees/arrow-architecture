package arrow.intro.runtime

import arrow.effects.fix

object IOApp {
  @JvmStatic
  fun main(args: Array<String>) {
    ClientApp(IORuntime).main().fix().unsafeRunSync()
  }
}