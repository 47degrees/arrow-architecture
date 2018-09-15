autoscale: false
slidenumbers: true
footer: [(@raulraja](https://twitter.com/raulraja) [, @47deg)](https://twitter.com/47deg) [=> Sources,](https://github.com/47deg/types-vs-tests) [Slides](https://speakerdeck.com/raulraja/types-vs-tests)

![inline](custom/images/arrow-brand-transparent.png)

# Building Apps and Libs with Λrrow

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
  - Support deeply nested relationship without boilerplate
3. Support __async non-blocking__ data types:
  - `Observable`, `Flux`, `Deferred` and `IO`
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
- Support deeply nested relationship without boilerplate

[.code-highlight: all]
[.code-highlight: 4, 7, 9, 11]

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
- Support deeply nested relationship without boilerplate

[.code-highlight: all]
[.code-highlight: 7-14, 17-21]

```kotlin:ank
import arrow.intro.*

val gist = 
  Gist(
    files = mapOf(
      "typeclassless_tagless_extensions.kt" to GistFile(
        fileName = "typeclassless_tagless_extensions.kt"
      )
    ),
    description = "Tagless with Arrow & typeclassless using extension functions and instances",
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
  - ~~Support deeply nested relationship without boilerplate~~
3. Support __async non-blocking__ data types:
  - `Observable`, `Flux`, `Deferred` and `IO`
4. __Pure__:
  - Never throw exceptions
  - Defer effects evaluation
  
--

## Support Async/Non-Blocking Popular data types

A initial naive blocking and exception throwing implementation

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

publicGistsForUser("raulraja")
```

---

## Support Async/Non-Blocking Popular data types

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

## Support Async/Non-Blocking Popular data types

Many choose to go non-blocking with Kotlin Coroutines, a great and popular kotlin async framework

```kotlin:fail
import kotlinx.coroutines.experimental.*

fun publicGistsForUser(userName: String): Deferred<Either<Throwable, ListK<Gist>>> =
  async {
    val (_, _, result) = "https://api.github.com/users/$userName/gists".httpGet().responseString() 
    when (result) {
      is Result.Failure -> result.getException().left()
      is Result.Success -> fromJson(result.value).right()
    }
  }

publicGistsForUser("raulraja")
```

---

## Support Async/Non-Blocking Popular data types

But now we have to dive deep into the `Deferred` and `Either` effects to get to the value we care about

```kotlin
import arrow.effects.*
import arrow.instances.*
import arrow.typeclasses.*
import arrow.effects.typeclasses.*

DeferredK.monad().binding {
  val raulResult: Either<Throwable, ListK<Gist>> = publicGistsForUser("raulraja").k().bind() 
  val rafaResult: Either<Throwable, ListK<Gist>> = publicGistsForUser("rafaparedela").k().bind()
  Either.monad<Throwable>().binding {
    val raulGists: ListK<Gist> = raulResult.bind()
    val rafaGists: ListK<Gist> = rafaResult.bind()
    raulGists + rafaGists
  }
}
```

---

## Support Async/Non-Blocking Popular data types

Arrow Monad Transformers allow you to remain in the world of concrete data types such as `Either` and `Deferred`

```kotlin
import arrow.effects.*

EitherT
  .monad<ForDeferredK, Throwable>(DeferredK.monad())
  .binding {
     val raulGists = EitherT(publicGistsForUser("raulraja").k()).bind()
     val rafaGists = EitherT(publicGistsForUser("rafaparedela").k()).bind()
     raulGists + rafaGists
   }
```

---

## Support Async/Non-Blocking Popular data types

But none of this stuff actually requires concrete data types if we use a Tagless Final Encoding.
`Kind<F, A>` is the same as an `A` value inside `F` where `F` is a container.
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
class DefaultGistApiDataSource<F>() : GistApiDataSource<F> {
  override fun publicGistsForUser(userName: String): Kind<F, ListK<Gist>> = TODO()
}
```

---

## Support Async/Non-Blocking Popular data types

Ad-Hoc Polymorphism and type classes!
A type class is a generic interface that describes behaviors that concrete types can support

```kotlin
interface Functor<F> {
  fun <A, B> Kind<F, A>.map(f: (A) -> B): Kind<F, B>
}
```

---

## Support Async/Non-Blocking Popular data types

Monad models transformations which depend on values to be completed.

```kotlin
interface Monad<F> : Applicative<F> {
  fun <A, B> Kind<F, A>.flatMap(f: (A) -> Kind<F, B>): Kind<F, B>
}
```

```kotlin
listOf(1).map { it + 1 }
```
```kotlin
Option(1).map { it + 1 }
```
```kotlin
Try { 1 }.map { it + 1 }
```
```kotlin
Either.Right(1).map { it + 1 }
```

---


## Applicative Builder

```kotlin
```

---

## Applicative Builder (Same for all data types)

```kotlin
```

---

## Comprehensions - Vanilla

Generalized to all monads. A suspended function provides a non blocking `F<A> -> A`

```kotlin
```

---

## Comprehensions - Exception Aware

Automatically captures exceptions for instances of `MonadError<F, Throwable>`

```kotlin
```

---

## Comprehensions - Filterable

Imperative filtering control for data types that can provide `empty` values.

```kotlin
```

---

## Integrations - Rx2

Let’s take an example and convert it to a comprehension. 

```kotlin
```

---

## Integrations - Rx2

Arrow provides `MonadError<F, Throwable>` for `Observable`

```kotlin
```

---

## Integrations - Kotlin Coroutines 

Arrow provides `MonadError<F, Throwable>` for `Deferred`

```kotlin
```

---

## Transforming immutable data

Λrrow includes an `optics` library that make working with immutable data a breeze

```kotlin
```

---

## Transforming immutable data

Λrrow includes an `optics` library that make working with immutable data a breeze

```kotlin
```

---

## Transforming immutable data

while `kotlin` provides a synthetic `copy` dealing with nested data can be tedious

```kotlin
```

---

## Optics without boilerplate

You may define composable `Lenses` to work with immutable data transformations

```kotlin
```

---

## Optics without boilerplate

You may define composable `Lenses` to work with immutable data transformations

```kotlin
```

---

## Optics without boilerplate

Or just let Λrrow `@optics` do the dirty work

```diff
```

---

## Optics without boilerplate

Optics comes with a succinct and powerful DSL to manipulate deeply nested immutable properties

```kotlin
```

---

## Optics without boilerplate

You can also define `@optics` for your sealed hierarchies

```kotlin
```

---

## Optics without boilerplate

Where you operate over sealed hierarchies manually...

```kotlin
```

---

## Optics without boilerplate

...you cruise now through properties with the new optics DSL

```kotlin
```

---

## In the works

|        |                                                 |
|--------|-------------------------------------------------|
| arrow-generic | Generic programming with products, coproducts and derivation |
| arrow-streams | A functional `Stream<F, A>` impl that abstract over F and complementes `arrow-effect` |
| arrow-android | FP solutions to common Android issues `Ex: Activity lifecycle` |
| bow | Arrow for iOS built on swift |

---

<!-- .slide: class="table-large" -->

## Λrrow is modular

Pick and choose what you'd like to use.

| Module            | Contents                                                              |
|-------------------|-----------------------------------------------------------------------|
| typeclasses       | `Semigroup`, `Monoid`, `Functor`, `Applicative`, `Monad`...                      |
| core/data              | `Option`, `Try`, `Either`, `Validated`...                                     |
| effects           | `IO`                                                                    |
| effects-rx2       | `ObservableKW`, `FlowableKW`, `MaybeK`, `SingleK`                                                          |
| effects-coroutines       | `DeferredK`                                                       |
| mtl               | `MonadReader`, `MonadState`, `MonadFilter`,...                              |
| free              | `Free`, `FreeApplicative`, `Trampoline`, ...                                |
| recursion-schemes | `Fix`, `Mu`, `Nu`                                                                     |
| optics            | `Prism`, `Iso`, `Lens`, ...                                                 |
| meta              | `@higherkind`, `@deriving`, `@instance`, `@optics` |

---

## Kotlin limitations for Typed FP

---

## Kotlin limitations for Typed FP

Emulated Higher Kinds through [Lightweight higher-kinded Polymorphism](https://www.cl.cam.ac.uk/~jdy22/papers/lightweight-higher-kinded-polymorphism.pdf)

---

## Kotlin limitations for Typed FP

Fear not, `@higherkind`'s got your back!

```diff
+ @higherkind sealed class Option<A> : OptionOf<A>
- class ForOption private constructor() { companion object }
- typealias OptionOf<A> = Kind<ForOption, A>
- inline fun <A, B> OptionOf<A>.fix(): Option<A> = this as Option<A>
```

---

## Λrrow ad-hoc polymorphism

With emulated Higher Kinds and Type classes we can now write polymorphic code

```kotlin
import arrow.Kind
import arrow.core.*
import arrow.effects.*
import arrow.typeclasses.*
```

---

## Λrrow ad-hoc polymorphism

With emulated Higher Kinds and Type classes we can now write polymorphic code

```kotlin
```

---

## Λrrow ad-hoc polymorphism

With emulated Higher Kinds and Type classes we can now write polymorphic code

```kotlin
```

---

## Λrrow ad-hoc polymorphism

With emulated Higher Kinds and Type classes we can now write polymorphic code

```kotlin
```

---

## Λrrow ad-hoc polymorphism

With emulated Higher Kinds and Type classes we can now write polymorphic code

```kotlin
```

---

## Λrrow ad-hoc polymorphism

Program that are abstract and work in many runtimes!

```kotlin
```

---

## Λrrow ad-hoc polymorphism

Program that are abstract and work in many runtimes!

```kotlin
```

---

## Λrrow ad-hoc polymorphism

Program that are abstract and work in many runtimes!

```kotlin
```

---

## Type Classes

This is how you define Type Classes in Λrrow (for now)

```kotlin
```

---

## Implementing type class instances is easy...

---

## @deriving

Λrrow can derive instances based on conventions in your data types

```kotlin

```

---

## @instance

Λrrow allows you to hand craft instances

```kotlin
```

---

## KEEP-87

But we are not stopping here, we want to get rid of some of the codegen.

KEEP-87 is A KEEP to introduce Type Classes in Kotlin!

https://github.com/Kotlin/KEEP/pull/87

---

## KEEP-87

Type Classes & Instances

```kotlin
interface Monoid<A> {
  fun A.combine(b: A): A
  val empty: A
}

extension object IntMonoid : Monoid<Int> {
  fun Int.combine(b: Int): Int = this + b
  val empty: Int = 0
}
```

---

## KEEP-87

Declaration site

```kotlin
fun combineOneAndTwo(with mi: Monoid<Int>) =
  1.combine(2) // `Monoid<Int>` is in scope
```

Desugars to

```kotlin
fun combineOneAndTwo(ev: Monoid<Int>) =
  with(ev) { 1.combine(2) } // `this` is ev
```

---

## KEEP-87

Call site

```kotlin
import IntMonoid
combineOneAndTwo() // instance is resolved via imports and injected by the compiler
```

Desugars to

```kotlin
import IntMonoid
combineOneAndTwo(IntMonoid) // compatible with java and allows explicit overrides
```

---

## An ecosystem of libraries

### __Λnk__ 

Markdown documentation, verification and snippet evaluator for Kotlin

<img src="custom/images/Ank.svg" alt="Λnk">

---

## An ecosystem of libraries

### __Helios__

A fast, purely functional JSON lib for Kotlin

<img src="custom/images/Helios.svg" alt="Helios">

---

## An ecosystem of libraries

### __Kollect__

Efficient data access with id dedup, parallelization, batching and caching.

<img src="custom/images/Kollect.svg" alt="Kollect">

---

## Credits

Λrrow is inspired in great libraries that have proven useful to the FP community:

- [Cats](https://typelevel.org/cats/)
- [Scalaz](https://github.com/scalaz/scalaz)
- [Freestyle](http://frees.io)
- [Monocle](http://julien-truffaut.github.io/Monocle/)
- [Funktionale](https://github.com/MarioAriasC/funKTionale)

---

<!-- .slide: class="team" -->

## 72 Contributors and counting

- [![](https://github.com/anstaendig.png) **anstaendig**](https://github.com/anstaendig)
- [![](https://github.com/arturogutierrez.png) **arturogutierrez**](https://github.com/arturogutierrez)
- [![](https://github.com/ffgiraldez.png) **ffgiraldez**](https://github.com/ffgiraldez)
- [![](https://github.com/Guardiola31337.png) **Guardiola31337**](https://github.com/Guardiola31337)
- [![](https://github.com/javipacheco.png) **javipacheco**](https://github.com/javipacheco)
- [![](https://github.com/JMPergar.png)  **JMPergar**](https://github.com/JMPergar)
- [![](https://github.com/JorgeCastilloPrz.png) **JorgeCastilloPrz**](https://github.com/JorgeCastilloPrz)
- [![](https://github.com/jrgonzalezg.png) **jrgonzalezg**](https://github.com/jrgonzalezg)
- [![](https://github.com/nomisRev.png) **nomisRev**](https://github.com/nomisRev)
- [![](https://github.com/npatarino.png) **npatarino**](https://github.com/npatarino)
- [![](https://github.com/pablisco.png) **pablisco**](https://github.com/pablisco)
- [![](https://github.com/pakoito.png)  **pakoito**](https://github.com/pakoito)
- [![](https://github.com/pedrovgs.png) **pedrovgs**](https://github.com/pedrovgs)
- [![](https://github.com/pt2121.png)   **pt2121**](https://github.com/pt2121)
- [![](https://github.com/raulraja.png) **raulraja**](https://github.com/raulraja)
- [![](https://github.com/wiyarmir.png) **wiyarmir**](https://github.com/wiyarmir)
- [![](https://github.com/andyscott.png) **andyscott**](https://github.com/andyscott)
- [![](https://github.com/Atternatt.png) **Atternatt**](https://github.com/Atternatt)
- [![](https://github.com/calvellido.png) **calvellido**](https://github.com/calvellido)
- [![](https://github.com/dominv.png) **dominv**](https://github.com/dominv)
- [![](https://github.com/GlenKPeterson.png) **GlenKPeterson**](https://github.com/GlenKPeterson)
- [![](https://github.com/israelperezglez.png) **israelperezglez**](https://github.com/israelperezglez)
- [![](https://github.com/sanogueralorenzo.png) **sanogueralorenzo**](https://github.com/sanogueralorenzo)
- [![](https://github.com/Takhion.png) **Takhion**](https://github.com/Takhion)
- [![](https://github.com/victorg1991.png) **victorg1991**](https://github.com/victorg1991)
- [![](https://github.com/tonilopezmr.png) **tonilopezmr**](https://github.com/tonilopezmr)
- [![](https://github.com/NigelHeylen.png) **NigelHeylen**](https://github.com/NigelHeylen)
- [![](https://github.com/ersin-ertan.png) **ersin-ertan**](https://github.com/ersin-ertan)


---

<!-- .slide: class="join-us" -->

## Join us!

|        |                                                 |
|--------|-------------------------------------------------|
| Github | https://github.com/arrow-kt/arrow                     |
| Slack  | https://kotlinlang.slack.com/messages/C5UPMM0A0 |
| Gitter | https://gitter.im/arrow-kt/Lobby               |

We provide 1:1 mentoring for both users & new contributors!

---

## Thanks!

Thanks to everyone that makes Λrrow possible

- [![](custom/images/47deg-logo.png)](https://www.47deg.com/)
- [![](custom/images/kotlin.png)](https://kotlinlang.org/)
- [![](custom/images/lw-logo.png)](http://www.lambda.world/)
- [![](custom/images/FineCinnamon.png)](https://github.com/FineCinnamon)
