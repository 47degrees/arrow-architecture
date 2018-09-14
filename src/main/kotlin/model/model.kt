package arrow.intro

import arrow.optics.optics
import com.squareup.moshi.JsonClass

@optics
@JsonClass(generateAdapter = true)
data class Gist(
  val url: String,
  val id: String,
  val files: Map<String, GistFile>,
  val description: String?,
  val comments: Long,
  val owner: GithubUser
) {
  companion object
}

@optics
@JsonClass(generateAdapter = true)
data class GithubUser(
  val login: String,
  val id: Long,
  val url: String
) {
  companion object
}

@optics
@JsonClass(generateAdapter = true)
data class GistFile(
  val fileName: String?,
  val type: String,
  val language: String?,
  val size: Long
) {
  companion object
}