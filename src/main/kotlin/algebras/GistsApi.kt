package arrow.intro.algebras

import arrow.Kind
import arrow.data.ListK
import arrow.intro.Gist

interface GistsApi<F> {
  fun publicGistsForUser(username: String): Kind<F, ListK<Gist>>
}

class DefaultGistApi<F>(private val dataSource: GistApiDataSource<F>) : GistsApi<F> {
  override fun publicGistsForUser(username: String): Kind<F, ListK<Gist>> =
    dataSource.publicGistsForUser(username)
}