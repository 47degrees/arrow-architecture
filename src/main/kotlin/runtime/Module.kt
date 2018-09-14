package arrow.intro.runtime

import arrow.effects.typeclasses.Async
import arrow.intro.algebras.*

abstract class Module<F>(
  val async: Async<F>,
  val logger: Logger<F> = DefaultConsoleLogger(async),
  private val dataSource: GistApiDataSource<F> = DefaultGistApiDataSource(async, logger),
  val api: GistsApi<F> = DefaultGistApi(dataSource)
)