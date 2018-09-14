package arrow.intro.runtime

import arrow.data.ListK
import arrow.effects.ForObservableK
import arrow.effects.ObservableK
import arrow.effects.async
import arrow.effects.fix
import arrow.intro.Gist
import io.reactivex.Observable

object Rx2Runtime : Module<ForObservableK>(ObservableK.async()) {
  fun publicGistsForUser(username: String): Observable<ListK<Gist>> =
    api.publicGistsForUser(username).fix().observable
}