package arrow.intro.runtime

import arrow.effects.ForIO
import arrow.effects.IO
import arrow.effects.async
import arrow.effects.fix
import arrow.intro.Gist

object IORuntime : Module<ForIO>(IO.async()) {
  fun publicGistsForUser(username: String): IO<List<Gist>> =
    api.publicGistsForUser(username).fix()
}