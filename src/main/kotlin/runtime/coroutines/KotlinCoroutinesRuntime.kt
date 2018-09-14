package arrow.intro.runtime

import arrow.effects.DeferredK
import arrow.effects.ForDeferredK
import arrow.effects.async
import arrow.effects.fix
import arrow.intro.Gist
import kotlinx.coroutines.experimental.Deferred

object KotlinCoroutinesRuntime : Module<ForDeferredK>(DeferredK.async()) {
  fun publicGistsForUser(username: String): Deferred<List<Gist>> =
    api.publicGistsForUser(username).fix().deferred
}