package arrow.intro.algebras

import arrow.Kind
import arrow.core.*
import arrow.data.ListK
import arrow.data.k
import arrow.effects.typeclasses.Async
import arrow.intro.Gist
import arrow.typeclasses.binding
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.*

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

  companion object {

    private val moshi: Moshi = Moshi.Builder()
      .add(KotlinJsonAdapterFactory())
      .build()

    private val gistListJsonAdapter: JsonAdapter<List<Gist>> =
      moshi
        .adapter(Types.newParameterizedType(List::class.java, Gist::class.java))

    private fun fromJson(json: String): ListK<Gist> =
      Try { gistListJsonAdapter.lenient().fromJson(json).toOption() }
        .fold({ emptyList<Gist>().k() }, { parsed ->
          parsed.fold({ emptyList<Gist>().k() }, { it.k() })
        })

  }
}