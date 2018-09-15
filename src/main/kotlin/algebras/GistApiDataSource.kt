package arrow.intro.algebras

import arrow.Kind
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import arrow.data.ListK
import arrow.effects.typeclasses.Async
import arrow.intro.Gist
import arrow.intro.fromJson
import arrow.typeclasses.binding
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result

interface GistApiDataSource<F> {
  fun publicGistsForUser(userName: String): Kind<F, ListK<Gist>>
}

class DefaultGistApiDataSource<F>(
  private val async: Async<F>,
  private val logger: Logger<F>
) : GistApiDataSource<F>, Async<F> by async {
  override fun publicGistsForUser(userName: String): Kind<F, ListK<Gist>> =
      binding {
        logger.log("Fetching gists for `$userName`").bind()
        async { proc: (Either<Throwable, ListK<Gist>>) -> Unit ->
          "https://api.github.com/users/$userName/gists".httpGet().responseString { _, _, result ->
            when (result) {
              is Result.Failure -> proc(result.getException().left())
              is Result.Success -> proc(fromJson(result.value).right())
            }
          }
        }.bind()
      }
}