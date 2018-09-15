package arrow.intro

import arrow.optics.optics
import com.squareup.moshi.JsonClass

@optics
@JsonClass(generateAdapter = true)
data class Gist(
  val files: Map<String, GistFile>,
  val description: String?,
  val comments: Long,
  val owner: GithubUser) {

  override fun toString(): String =
    "Gist($description, ${owner.login}, file count: ${files.size})"

}

@optics
@JsonClass(generateAdapter = true)
data class GithubUser(val login: String)

@optics
@JsonClass(generateAdapter = true)
data class GistFile(val fileName: String?)
