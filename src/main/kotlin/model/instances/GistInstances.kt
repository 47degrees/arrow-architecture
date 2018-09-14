package model.instances

import arrow.intro.Gist
import arrow.typeclasses.Show

object GistShowInstance : Show<Gist> {
  override fun Gist.show(): String =
    """|[$description] by [${owner.login}] :
       |  files : ${files.toList().joinToString("") { (name, file) -> "\n    $name (${file.size} bytes) (lang: ${file.language ?: "none"})" }}
       |
    """.trimMargin()
}

fun Gist.Companion.show(): Show<Gist> = GistShowInstance