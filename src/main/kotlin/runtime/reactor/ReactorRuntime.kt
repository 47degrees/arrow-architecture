package arrow.intro.runtime

import arrow.data.ListK
import arrow.effects.FluxK
import arrow.effects.ForFluxK
import arrow.effects.async
import arrow.effects.fix
import arrow.intro.Gist
import reactor.core.publisher.Flux

object ReactorRuntime : Module<ForFluxK>(FluxK.async()) {
  fun publicGistsForUser(username: String): Flux<ListK<Gist>> =
    api.publicGistsForUser(username).fix().flux
}