package arrow.intro

import arrow.core.*
import arrow.data.ListK
import arrow.data.k
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking

private val moshi: Moshi = Moshi.Builder()
  .add(KotlinJsonAdapterFactory())
  .build()

val gistListJsonAdapter: JsonAdapter<List<Gist>> =
  moshi.adapter(Types.newParameterizedType(List::class.java, Gist::class.java))

fun fromJson(json: String): ListK<Gist> =
  Try { gistListJsonAdapter.lenient().fromJson(json).toOption() }
    .fold({ emptyList<Gist>().k() }, { parsed ->
      parsed.fold({ emptyList<Gist>().k() }, { it.k() })
    })

fun publicGistsForUser(userName: String): Deferred<Either<Throwable, ListK<Gist>>> =
  async {
    val (_, _, result) = "https://api.github.com/users/$userName/gists".httpGet().responseString()
    when (result) {
      is Result.Failure -> result.getException().left()
      is Result.Success -> fromJson(result.value).right()
    }
  }

val x = runBlocking { publicGistsForUser("raulraja") }