autoscale: true
slidenumbers: true
footer:/           [@raulraja](https://twitter.com/raulraja) -> [@47deg](https://twitter.com/47deg) -> [Sources](https://github.com/47deg/arrow-architecture) -> [Slides](https://speakerdeck.com/raulraja/arrow-architecture)

# Building Apps & Libraries with Λrrow

![inline](custom/images/arrow-brand-transparent.png)

---

# Who am I? # 

![](custom/images/cover-background.png)

[@raulraja](https://twitter.com/raulraja)
[@47deg](https://twitter.com/47deg)

- Co-Founder and CTO at 47 Degrees
- FP advocate
- Electric Guitar @ <Ben Montoya & the Free Monads>

---

## Started as learning Exercise to learn FP over Slack

![inline](custom/images/ojetehandler.png)

---

## ...then KΛTEGORY was born: Solution for Typed FP in Kotlin

![inline](custom/images/kategory-logo.svg)

---

## KΛTEGORY + Funktionale = Λrrow

![inline](custom/images/arrow-brand-transparent.png)

---

## Type classes

Λrrow contains many FP related type classes

|                |                                                      |
|----------------|------------------------------------------------------|
| Error Handling | `ApplicativeError`,`MonadError`                      |
| Computation    | `Functor`, `Applicative`, `Monad`, `Bimonad`, `Comonad`                    |
| Folding        | `Foldable`, `Traverse`                          |
| Combining      | `Semigroup`, `SemigroupK`, `Monoid`, `MonoidK` |
| Effects        | `MonadDefer`, `Async`, `Effect`           |
| Recursion      | `Recursive`, `BiRecursive`,...                                |
| MTL            | `FunctorFilter`, `MonadState`, `MonadReader`, `MonadWriter`, `MonadFilter`, ...                |

---

## Data types

Λrrow contains many data types to cover general use cases.

|                |                                                      |
|----------------|------------------------------------------------------|
| Error Handling | `Option`,`Try`, `Validated`, `Either`, `Ior`         |
| Collections    | `ListKW`, `SequenceKW`, `MapKW`, `SetKW`             |
| RWS            | `Reader`, `Writer`, `State`                          |
| Transformers   | `ReaderT`, `WriterT`, `OptionT`, `StateT`, `EitherT` |
| Evaluation     | `Eval`, `Trampoline`, `Free`, `FunctionN`            |
| Effects        | `IO`, `Free`, `ObservableKW`                         |
| Optics         | `Lens`, `Prism`, `Iso`,...                           |
| Recursion      | `Fix`, `Mu`, `Nu`,...                                |
| Others         | `Coproduct`, `Coreader`, `Const`, ...                |

---

## Let's build a simple library

### Requirements
1. __Fetch Gists__ information __given a github user__ 
2. __Immutable__ model
  - Allow easy in memory updates
  - Support deeply nested relationships without boilerplate
3. Support __async non-blocking__ data types:
  - `Observable`, `Flux`, `Deferred` and `IO`
  - Allow easy access to nested effects
4. __Pure__:
  - Never throw exceptions
  - Defer effects evaluation

---

## __Fetch Gists__ information __given a github user__ 

```kotlin
fun publicGistsForUser(userName: String): List<Gist> = TODO()
```

---

## __Immutable__ model

- Allow easy in memory updates
- Support deeply nested relationships without boilerplate

```kotlin
data class Gist(
  val files: Map<String, GistFile>,
  val description: String?,
  val comments: Long,
  val owner: GithubUser) {
  
  override fun toString(): String =
    "Gist($description, ${owner.login}, file count: ${files.size})"  
    
}

data class GithubUser(val login: String) 

data class GistFile(val fileName: String?)
```

---

## __Immutable__ model

- Allow easy in memory updates
- Support deeply nested relationships without boilerplate

```kotlin:ank
import arrow.intro.*

val gist = 
  Gist(
    files = mapOf(
      "typeclassless_tagless_extensions.kt" to GistFile(
        fileName = "typeclassless_tagless_extensions.kt"
      )
    ),
    description = "Tagless with Λrrow & typeclassless using extension functions and instances",
    comments = 0,
    owner = GithubUser(login = "raulraja")
  )
```

---

## __Immutable__ model

The `data class` synthetic `copy` is fine for simple cases

```kotlin:ank
gist.copy(description = gist.description?.toUpperCase())
```

---

## __Immutable__ model

As we dive deeper to update nested data the levels of nested `copy` increases

```kotlin:ank
gist.copy(
  owner = gist.owner.copy(
    login = gist.owner.login.toUpperCase()
  )
)
```

---

## __Immutable__ model

In Typed FP immutable updates is frequently done with composable `Optics` like `Lens`

```kotlin:ank
import arrow.optics.*

val ownerLens: Lens<Gist, GithubUser> = 
  Lens(
    get = { gist -> gist.owner },
    set = { value -> { gist: Gist -> gist.copy(owner = value) }}
  )
  
val loginLens: Lens<GithubUser, String> = 
  Lens(
    get = { user -> user.login },
    set = { value -> { user -> user.copy(login = value) }}
  )
  
val ownerLogin = ownerLens compose loginLens

ownerLogin.modify(gist, String::toUpperCase)
```

---

## __Immutable__ model

Updating arbitrarily nested data with Λrrow is a piece of cake

```kotlin
@optics
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
```

---

## Provide an immutable data model and means to update it

Updating arbitrarily nested data with Λrrow is a piece of cake

```diff
- val ownerLens: Lens<Gist, GithubUser> = 
-  Lens(
-    get = { gist -> gist.owner },
-    set = { value -> { gist: Gist -> gist.copy(owner = value) }}
-  ) 
- val loginLens: Lens<GithubUser, String> = 
-  Lens(
-    get = { user -> user.login },
-    set = { value -> { user -> user.copy(login = value) }}
-  )
- val ownerLogin = ownerLens compose loginLens
- ownerLogin.modify(gist, String::toUpperCase)
+ import arrow.optics.dsl.*
+ Gist.owner.login.modify(gist, String::toUpperCase)
```

---

## Let's build a simple library

### Requirements
1. __Fetch Gists__ information __given a github user__ 
2. ~~__Immutable__ model~~
  - ~~Allow easy in memory updates~~
  - ~~Support deeply nested relationships without boilerplate~~
3. Support __async non-blocking__ data types:
  - `Observable`, `Flux`, `Deferred` and `IO`
  - Allow easy access to nested effects
4. __Pure__:
  - Never throw exceptions
  - Defer effects evaluation

---

## Support Async/Non-Blocking Popular data types

A initial impure implementation that blocks and throws exception

```kotlin:ank
import arrow.intro.Gist
import arrow.data.*
import com.squareup.moshi.*
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result

fun publicGistsForUser(userName: String): ListK<Gist> {
  val (_,_, result) = "https://api.github.com/users/$userName/gists".httpGet().responseString() // blocking IO
  return when (result) {
    is Result.Failure -> throw result.getException() // blows the stack
    is Result.Success -> fromJson(result.value)
  }
}
```

---

## Let's build a simple library

### Requirements
1. ~~__Fetch Gists__ information __given a github user__~~
2. ~~__Immutable__ model~~
  - ~~Allow easy in memory updates~~
  - ~~Support deeply nested relationships without boilerplate~~
3. Support __async non-blocking__ data types:
  - `Observable`, `Flux`, `Deferred` and `IO`
  - Allow easy access to nested effects
4. __Pure__:
  - Never throw exceptions
  - Defer effects evaluation

---

## Don't throw exceptions

When you are learning FP one leans toward using exception-free but blocking `Try` and `Either` like types.

```kotlin:ank
import arrow.core.*

fun publicGistsForUser(userName: String): Either<Throwable, ListK<Gist>> {
  val (_,_, result) = "https://api.github.com/users/$userName/gists".httpGet().responseString() // blocking IO
  return when (result) {
    is Result.Failure -> result.getException().left() //exceptions as a value
    is Result.Success -> fromJson(result.value).right()
  }
}

publicGistsForUser("-__unkown_user__-")
```

---

## Let's build a simple library

### Requirements
1. ~~__Fetch Gists__ information __given a github user__~~
2. ~~__Immutable__ model~~
  - ~~Allow easy in memory updates~~
  - ~~Support deeply nested relationships without boilerplate~~
3. Support __async non-blocking__ data types:
  - `Observable`, `Flux`, `Deferred` and `IO`
  - Allow easy access to nested effects
4. __Pure__:
  - ~~Never throw exceptions~~
  - Defer effects evaluation

---

## Support Async/Non-Blocking Popular data types

Many choose to go non-blocking with Kotlin Coroutines, a great and popular kotlin async framework

```kotlin:ank
import kotlinx.coroutines.experimental.*

fun publicGistsForUser(userName: String): Deferred<Either<Throwable, ListK<Gist>>> =
  async {
    val (_, _, result) = "https://api.github.com/users/$userName/gists".httpGet().responseString() 
    when (result) {
      is Result.Failure -> result.getException().left()
      is Result.Success -> fromJson(result.value).right()
    }
  }
  
//by default `async` when constructed runs and does not suspend effects  
publicGistsForUser("-__unkown_user1__-") 
```

---

## Let's build a simple library

### Requirements
1. ~~__Fetch Gists__ information __given a github user__~~
2. ~~__Immutable__ model~~
  - ~~Allow easy in memory updates~~
  - ~~Support deeply nested relationships without boilerplate~~
3. Support __async non-blocking__ data types:
  - `Observable`, `Flux`, ~~Deferred~~ and `IO`
  - Allow easy access to nested effects
4. __Pure__:
  - ~~Never throw exceptions~~
  - Defer effects evaluation

---

## Support Async/Non-Blocking Popular data types

But now we have to dive deep into the `Deferred` and `Either` effects to get to the value we care about

```kotlin
suspend fun allGists(): List<Gist> {
  val raulResult: Either<Throwable, ListK<Gist>> = publicGistsForUser("-__unkown_user1__-").await() 
  val rafaResult: Either<Throwable, ListK<Gist>> = publicGistsForUser("-__unkown_user2__-").await()
  return when {
    raulResult is Either.Right && rafaResult is Either.Right ->
      raulResult.b + rafaResult.b
    else ->
      emptyList<Gist>()
  }
}
```

---

## Support Async/Non-Blocking Popular data types

Λrrow Monad Transformers allow you to remain in the world of concrete data types such as `Either` and `Deferred`

```kotlin:ank
import arrow.effects.*
import arrow.instances.*
import arrow.typeclasses.*
import arrow.effects.typeclasses.*

fun allGists(): DeferredK<Either<Throwable, List<Gist>>> = 
  EitherT
    .monad<ForDeferredK, Throwable>(DeferredK.monad())
    .binding {
       val raulGists = EitherT(publicGistsForUser("-__unkown_user1__-").k()).bind()
       val rafaGists = EitherT(publicGistsForUser("-__unkown_user2__-").k()).bind()
       raulGists + rafaGists
   }.value().fix()

// Λrrow's delegation to `async` is always lazy
allGists()
```

---

## Let's build a simple library

### Requirements
1. ~~__Fetch Gists__ information __given a github user__~~
2. ~~__Immutable__ model~~
  - ~~Allow easy in memory updates~~
  - ~~Support deeply nested relationships without boilerplate~~
3. Support __async non-blocking__ data types:
  - `Observable`, `Flux`, ~~Deferred~~ and `IO`
  - ~~Allow easy access to nested effects~~
4. ~~__Pure__:~~
  - ~~Never throw exceptions~~
  - ~~Defer effects evaluation~~

---

## Support Async/Non-Blocking Popular data types

What about supporting `Observable`, `Flux`, `IO` and other unforeseen user data types?

---

## Support Async/Non-Blocking Popular data types

Turns out we don't need concrete data types if we use Type classes and Polymorphism

---

## Support Async/Non-Blocking Popular data types

Λrrow can abstract away the computational container type emulating __higher kinded types__.

`Kind<F, A>` denotes an `A` value inside an `F` type contructor:
Ex: `List<A>`, `Deferred<A>`, `IO<A>`, `Observable<A>`

```kotlin
import arrow.Kind

interface GistApiDataSource<F> {
  fun publicGistsForUser(userName: String): Kind<F, ListK<Gist>>
}
```

---

## Support Async/Non-Blocking Popular data types

How can we implement a computation in the context of `F` if we don't know what `F` is?

```kotlin
class DefaultGistApiDataSource<F> : GistApiDataSource<F> {
  override fun publicGistsForUser(userName: String): Kind<F, ListK<Gist>> = TODO()
}
```

---

## Support Async/Non-Blocking Popular data types

Ad-Hoc Polymorphism and type classes!
A type class is a generic interface that describes behaviors that concrete types can support

```kotlin
interface Functor<F> {
  // Λrrow projects type class behaviors as static or extension functions over kinded values
  fun <A, B> Kind<F, A>.map(f: (A) -> B): Kind<F, B>
  fun <A, B> lift(f: (A) -> B): (Kind<F, A>) -> Kind<F, B> =
      { fa: Kind<F, A> -> fa.map(f) }
}
```

---

## Support Async/Non-Blocking Popular data types

For example `Functor` allows us to transform the contents regardless of the concrete data type.

```kotlin:ank
listOf(1).map { it + 1 }
```
```kotlin:ank
Option(1).map { it + 1 }
```
```kotlin:ank
Try { 1 }.map { it + 1 }
```
```kotlin:ank
Either.Right(1).map { it + 1 }
```

---

## Support Async/Non-Blocking Popular data types

### Λrrow includes a comprehensive list of type classes

| Type class | Combinator |
| --- | --- |
| Semigroup | combine | 
| Monoid | empty | 
| Functor | map, lift | 
| Foldable | foldLeft, foldRight | 
| Traverse | traverse, sequence | 
| Applicative | pure, ap | 
| ApplicativeError | raiseError, catch | 
| Monad | flatMap, flatten | 
| MonadError | ensure, rethrow | 
| MonadDefer | delay, suspend | 
| Async | async | 
| Effect | runAsync | 

---

## Support Async/Non-Blocking Popular data types

We can use the `Async` type class to lift async computations into the abstract context of `F`

```kotlin
class DefaultGistApiDataSource<F>(private val async: Async<F>) : GistApiDataSource<F>, Async<F> by async {
  override fun publicGistsForUser(userName: String): Kind<F, ListK<Gist>> =
    async { proc: (Either<Throwable, ListK<Gist>>) -> Unit ->
      "https://api.github.com/users/$userName/gists".httpGet().responseString { _, _, result ->
        when (result) {
          is Result.Failure -> proc(result.getException().left())
          is Result.Success -> proc(fromJson(result.value).right())
         }
      }
    }
}
```

---

## Support Async/Non-Blocking Popular data types

If we have more than one logical services we can group them into a module

```kotlin
abstract class Module<F>(
  val async: Async<F>,
  val logger: Logger<F> = DefaultConsoleLogger(async),
  private val dataSource: GistApiDataSource<F> = DefaultGistApiDataSource(async, logger),
  val api: GistsApi<F> = DefaultGistApi(dataSource)
)
```

---

## Support Async/Non-Blocking Popular data types

Our library now supports all data types that provide a type class instance for `Async`.
This pattern allow you to keep code in a single place while providing

```
compile "com.biz:mylib-coroutines:$version"
object KotlinCoroutinesRuntime : Module<ForDeferredK>(DeferredK.async())
```

---

## Support Async/Non-Blocking Popular data types

Our library now supports all data types that provide a type class instance for `Async`.
This pattern allow you to keep code in a single place while providing

```
compile "com.biz:mylib-reactor:$version"
object ReactorRuntime : Module<ForFluxK>(FluxK.async())
```

---

## Support Async/Non-Blocking Popular data types

Our library now supports all data types that provide a type class instance for `Async`.
This pattern allow you to keep code in a single place while providing

```
compile "com.biz:mylib-arrow-io:$version"
object IORuntime : Module<ForIO>(IO.async())
```

---

## Support Async/Non-Blocking Popular data types

Our library now supports all data types that provide a type class instance for `Async`.
This pattern allow you to keep code in a single place while providing

```
compile "com.biz:mylib-rx2:$version"
object Rx2Runtime : Module<ForObservableK>(ObservableK.async())
```

---

## Let's build a simple library

### Requirements
1. ~~__Fetch Gists__ information __given a github user__~~
2. ~~__Immutable__ model~~
  - ~~Allow easy in memory updates~~
  - ~~Support deeply nested relationships without boilerplate~~
3. ~~Support __async non-blocking__ data types:~~
  - ~~Observable, Flux, Deferred and IO~~
  - ~~Allow easy access to nested effects~~
4. ~~__Pure__:~~
  - ~~Never throw exceptions~~
  - ~~Defer effects evaluation~~

---

## Recap

### Requirements
1. FUNC REQ ~~__Fetch Gists__ information __given a github user__~~
2. OPTICS ~~__Immutable__ model~~
  - ~~Allow easy in memory updates~~
  - ~~Support deeply nested relationships without boilerplate~~
3. POLYMORPHISM ~~Support __async non-blocking__ data types:~~
  - ~~Observable, Flux, Deferred and IO~~
  - ~~Allow easy access to nested effects~~
4. EFFECT CONTROL ~~__Pure__:~~
  - ~~Never throw exceptions~~
  - ~~Defer effects evaluation~~

---

## Λrrow is modular

Pick and choose what you'd like to use.

| Module            | Contents                                                              |
|-------------------|-----------------------------------------------------------------------|
| typeclasses       | `Semigroup`, `Monoid`, `Functor`, `Applicative`, `Monad`...                      |
| core/data         | `Option`, `Try`, `Either`, `Validated`...                                     |
| effects           | `Async`, `MonadDefer`, `Effect`, `IO`...                                                                    |
| effects-rx2       | `ObservableKW`, `FlowableKW`, `MaybeK`, `SingleK`                                                          |
| effects-coroutines       | `DeferredK`                                                       |
| mtl               | `MonadReader`, `MonadState`, `MonadFilter`,...                              |
| free              | `Free`, `FreeApplicative`, `Trampoline`, ...                                |
| recursion-schemes | `Fix`, `Mu`, `Nu`                                                                     |
| optics            | `Prism`, `Iso`, `Lens`, ...                                                 |
| meta              | `@higherkind`, `@deriving`, `@extension`, `@optics` |

---

## What we did not cover in this talk

- `binding` (do notation generalized to all Monads)
- `map` (applicative builder that preserves arity)
- How higher kinds are emulated
- Many type classes and modules to practice Typed FP in Kotlin
- The current state of type classes in Kotlin: [KEEP-87](https://github.com/Kotlin/KEEP/pull/87)

---

## Credits

Λrrow is inspired in great libraries that have proven useful to the FP community:

- [Cats](https://typelevel.org/cats/)
- [Scalaz](https://github.com/scalaz/scalaz)
- [Freestyle](http://frees.io)
- [Monocle](http://julien-truffaut.github.io/Monocle/)
- [Funktionale](https://github.com/MarioAriasC/funKTionale)

---

## Join us!

|        |                                                 |
|--------|-------------------------------------------------|
| Github | https://github.com/arrow-kt/arrow                     |
| Slack  | https://kotlinlang.slack.com/messages/C5UPMM0A0 |
| Gitter | https://gitter.im/arrow-kt/Lobby               |

We provide 1:1 mentoring for both users & new contributors!
+90 Contributors and growing!

---

## Thanks!

### Thanks to everyone that makes Λrrow possible!

![inline 80%](custom/images/47deg-logo.png)![inline 80%](custom/images/kotlin.png)![inline 80%](custom/images/lw-logo.png)