package arrow.intro.runtime

import arrow.effects.fix

object ObservableApp {
  @JvmStatic
  fun main(args: Array<String>) {
    ClientApp(Rx2Runtime).main().fix().observable.blockingFirst()
  }
}