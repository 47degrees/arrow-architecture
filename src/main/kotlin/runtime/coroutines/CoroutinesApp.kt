package arrow.intro.runtime

import arrow.effects.fix
import kotlinx.coroutines.experimental.runBlocking

object CoroutinesApp {
  @JvmStatic
  fun main(args: Array<String>): Unit {
    runBlocking {
      ClientApp(KotlinCoroutinesRuntime).main().fix().deferred.await()
    }
  }
}