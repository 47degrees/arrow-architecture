package arrow.intro.runtime

import arrow.effects.fix

object ReactorApp {
  @JvmStatic
  fun main(args: Array<String>) {
    ClientApp(ReactorRuntime).main().fix().flux.blockFirst()
  }
}