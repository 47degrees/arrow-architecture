package arrow.intro.runtime

import arrow.Kind
import arrow.data.ListK
import arrow.data.k
import arrow.effects.typeclasses.MonadDefer
import arrow.intro.Gist
import arrow.intro.algebras.GistsApi
import arrow.typeclasses.binding

class ClientApp<F>(
  private val module: Module<F>,
  private val delay: MonadDefer<F> = module.async
) : GistsApi<F> by module.api, MonadDefer<F> by delay {

  private fun gistsTeletype(): Kind<F, ListK<Gist>> =
    binding {
      delay { println("Enter github user name") }.bind()
      defer {
        val userInput = readLine()
        if (userInput != null && userInput.isNotBlank()) publicGistsForUser(userInput.trim())
        else gistsTeletype()
      }.bind()
    }

  fun main(): Kind<F, Unit> =
    binding {
      gistsTeletype().bind().k().traverse(this) { gist ->
        delay { println(gist) }
      }.followedBy(main()).followedBy(just(Unit)).bind()
    }

}


