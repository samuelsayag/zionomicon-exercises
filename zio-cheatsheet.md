The ZIO Cheatsheet
===================

## General considerations

### Why would you want to use ZIO

You want:

- scalability (uses `Fiber`s instead of `Thread`s)
- asynchronous / non-blocking behavior
- parallelisation to a high degree
- concurrency supported (a family of Reference) and STM (Software Transactional Memory)
- leak-free (high resource management support)
- efficiency
- streaming
- deterministic testability without using external system

At programing level: 
- a program is a value just as: `3`, `List(5)`, `Some(7)`...which as tremendous consequences on capabilities it gives.
- it provide a **unified** construct to deal with any effectful program.
- A **huge collection** of combinator to build program that answer complex problems
- A simple usable answer to data injection in the form of `ZLayer`s
- debugging (tracing over all `Fiber`s contrary to `Thread` that "swallow" the stack trace)

### The language of ZIO
One of the key to master `ZIO` is to understand its language.

The essence of the API is to have a _few number of types_ (as few as possible and very well designed) with a LOT of combinators.
+ The combinators names between the types follow some conventions such as `map`, `flatMap`, `fold`, `zip` etc. 
+ The most commonly encountered combinators have _symbol_ aliases `flatMap` <=> `>>=`, `zip` <=> `<*>` etc.
+ when the combinator finish by `M` (ex: `fold` and `foldM`) it signals that the parameter is either and effect (`M` for Monad) or producing and effect and will `flatten` it.
+ The combinators bridge the type with the standard Scala API whenever possible (`Option`, `Either`, `Future`).  
+ The combinators try to give relations between types of the ZIO library when possible.

### Convention in this sheet

You will notice that this cheatsheet gives almost no information about type for a given combinator.

Indeed it leverage the fact that ZIO is written above a very small well designed number of types (other like function are not specific to ZIO of course) that compose incredibly well.

Consequently, the below notation is enumerating these essential types together with a short way of denoting them.

It reads more easily and allows to throw away some complexity of the signature that practice of the library will anyway eliminate.

+ Simple value, often a effect (not wrapped) `a`
+ Effect of type ZIO are call `e`, `e1`, `e2`... 
+ Collection, noted `c` are taken in a broad sense when functions apply to them. They can be: `Option`, `Set`, `Chunk`, `Array`, `Collection <: Iterable`. This document just use the type `Collection` that do not exist per se but allows to write `Collection[A]` and intends all the concrete types cited above. 
+ Collection of effect (in the same sense than above) of effect are noted `ce`
+ function is noted `f` (A => B), `fm` (funtion the return effect: A => ZIO[...])
+ zio.duration is noted `d`
+ ZSchedule is noted `s`
+ ZScope is noted `zs`
+ ZManaged is noted `zm`
+ ExecutionContext (scala) `ec`, Executor `ex`
+ Failing values: Error `er`, Throwable `t`, Cause `ce`
+ Exit values `ex`

## The `ZIO` type

### Aliases

|      Alias     	|           Type           	|
|:--------------:	|:------------------------:	|
|   `IO[+E,+A]`  	|     `ZIO[Any, E, A]`     	|
|   `Task[+A]`   	| `ZIO[Any, Throwable, A]` 	|
|  `RIO[-R,+A]`  	|  `ZIO[R, Throwable, A]`  	|
|    `UIO[+A]`   	|  `ZIO[Any, Nothing, A]`  	|
|  `URIO[-R,+A]` 	|   `ZIO[R, Nothing, A]`   	|
| `Canceler[-R]` 	|  `ZIO[R, Nothing, Any]`  	|

### Building effects

### Using effects

The `ZIO[-R,+E,+A]` type is central to the whole API and is extremelly rich in combinators.

They have different purpose and can be roughly categorized as follows:
+ Interact with time/repetitions
+ Compose sequentially executed effects
+ Compose parallely executed effect
+ Compose concurrent effect
+ Act on the error channel
+ Act on the environment channel
+ Async API
+ Provide managed resources (resource, cache, layer...)

| Type                                                         	| ZIO[-R,+E,+A] 	|                                                                                                                                                                                                                                                                	|
|--------------------------------------------------------------	|---------------	|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------	|
| **Interact with time**                                       	|               	|                                                                                                                                                                                                                                                                	|
|                         _combinator_                         	|   _synonym_   	| _definition_                                                                                                                                                                                                                                                   	|
|                            `never`                           	|               	| equivalent to never ending loop but without resource consumption                                                                                                                                                                                               	|
|                           `forever`                          	|               	| execute the effect forever                                                                                                                                                                                                                                     	|
|                            `once`                            	|               	| will execute this effect once even if evaluated multiple times                                                                                                                                                                                                 	|
|                         `eventually`                         	|               	| repeat the effect until it fails                                                                                                                                                                                                                               	|
|  `retry s` <br/><br>`retryUntil...` <br/><br>`retryWhile...` 	|               	| A family of combinator whose essence is to retry the effect with some additional conditions/parameters                                                                                                                                                         	|
|                           `delay d`                          	|               	| delay the effect with a given duration                                                                                                                                                                                                                         	|
| `repeat` <br/><br>`repeatUntil...` <br/><br>`repeatWhile...` 	|               	| A family of combinators whose essence is to repeat the effect with some additional conditions/parameters                                                                                                                                                       	|
|           `schedule s` <br/><br>`scheduleFrom v s`           	|               	| Run the effect                                                                                                                                                                                                                                                 	|
| **Sequential execution**                                     	|               	|                                                                                                                                                                                                                                                                	|
|                         _combinator_                         	|   _synonym_   	| _definition_                                                                                                                                                                                                                                                   	|
|                        `e1 flatMap e2`                       	|  `e1 >>= e2`  	| execute e1 pass it to e2 and form a new effect with result                                                                                                                                                                                                     	|
|                          `e1 zip e2`                         	|  `e1 <*> e2`  	| Sequentially execute e1 and e2 and combine the result into a tuple                                                                                                                                                                                             	|
|                       `e1 zipRight e2`                       	|   `e1 *> e2`  	| idem but keep e2                                                                                                                                                                                                                                               	|
|                        `e1 zipLeft e2`                       	|   `e1 <* e2`  	| idem but keep e1                                                                                                                                                                                                                                               	|
|                         `foreach c f`                        	|               	| apply f to the value of c and wrapped the collection in an effect                                                                                                                                                                                              	|
|                        `collectAll ce`                       	|               	| transform a collection of effect to an effect of collection                                                                                                                                                                                                    	|
| **Parallel execution**                                       	|               	|                                                                                                                                                                                                                                                                	|
|                         _combinator_                         	|   _synonym_   	| _definition_                                                                                                                                                                                                                                                   	|
|                        `e1 zipPar e2`                        	|  `e1 <&> e2`  	| Parallely execute e1 and e2 and combine the result into a tuple                                                                                                                                                                                                	|
|                      `e1 zipParRight e2`                     	|   `e1 &> e2`  	| idem but keep e2                                                                                                                                                                                                                                               	|
|                      `e1 zipParLeft e2`                      	|   `e1 <& e2`  	| idem but keep e1                                                                                                                                                                                                                                               	|
|           `foreachPar c f` <br> `foreachParN n c f`          	|               	| apply f to the value of c and wrapped the collection in an effect.<br>N is the degree of parallelism.                                                                                                                                                          	|
|           `collectAllPar ce`<br> `collectParN n ce`          	|               	| transform a collection of effect to an effect of collection. N is the degree of parallelism.                                                                                                                                                                   	|
| **Racing execution**                                         	|               	|                                                                                                                                                                                                                                                                	|
|                         _combinator_                         	|   _synonym_   	| _definition_                                                                                                                                                                                                                                                   	|
|                      `e1 raceEither e2`                      	|  `e1 <\|> e2` 	| return the first to succeed as Either[A,B]                                                                                                                                                                                                                     	|
|                         `e1 race e2`                         	|               	| return the first to succeed if they are of the same type                                                                                                                                                                                                       	|
|                       `e1 raceFist e2`                       	|               	| return the first to finish (whatever result E/A, if E all the Cause[E] are present)                                                                                                                                                                            	|
|                          `raceAll c`                         	|               	| return the first effect to succeed when given a collection of effect.                                                                                                                                                                                          	|
| **Error channel**                                            	|               	|                                                                                                                                                                                                                                                                	|
|                         _combinator_                         	|   _synonym_   	| _definition_                                                                                                                                                                                                                                                   	|
|                       `flatMapError e`                       	|               	| flatMap on the error channel                                                                                                                                                                                                                                   	|
| **Async API**                                                	|               	|                                                                                                                                                                                                                                                                	|
|                         _combinator_                         	|   _synonym_   	| _definition_                                                                                                                                                                                                                                                   	|
|                            `fork`                            	|               	| _fork an effect_ in the local (z)scope                                                                                                                                                                                                                         	|
|                         `forkDaemon`                         	|               	| _fork an effect_ the global scope (daemon behavior)                                                                                                                                                                                                            	|
|                          `forkIn zs`                         	|               	| _fork an effect_ in the given scope                                                                                                                                                                                                                            	|
|                          `forkOn ec`                         	|               	| _fork an effect_ in the specified execution context                                                                                                                                                                                                            	|
|                           `lock ex`                          	|               	| _fork an effect_ in the specified executor`                                                                                                                                                                                                                    	|
|                       `forkManaged zm`                       	|               	| _fork and effect_ in the given `ZManaged`                                                                                                                                                                                                                      	|
|                            `in zs`                           	|               	| _modify the scope of the effect by_ extending the scope of the effect to the given scope                                                                                                                                                                       	|
|                    `overrideForkScope zs`                    	|               	| _modify the scope of the effect by_ **replacing** the scope of the effect by the given scope                                                                                                                                                                   	|
|                       `resetForkScope`                       	|               	| _modify the scope of the effect by_ resetting the effect scope to the scope of the current Fiber                                                                                                                                                               	|
|                          `forkScope`                         	|               	| _return the scope_ that will be used to fork effect in this effect                                                                                                                                                                                             	|
|                        `scopeWith fm`                        	|               	| _will pass to the given callback_ the scope of the current fiber                                                                                                                                                                                               	|
|                      `forkScopeWith fm`                      	|               	| _will pass to the given callback_ the scope that will be used to fork effect in this effect                                                                                                                                                                    	|
|                         `transplant`                         	|               	| _will pass to the given callback_ a handler (`grafter`)<br>that will override the scope of any effect with the scope of the current effect                                                                                                                     	|
|                         `ensuring e`                         	|               	| Register a finalizer `e` to be executed _no matter!_ how this effect is ending.                                                                                                                                                                                	|
|                          `interrupt`                         	|               	| Interrupt the Fiber that is running the effect.<br>Will interrupt any fiber whose ZScope inherit from the current ZScope automatically!                                                                                                                        	|
|                        `interruptible`                       	|               	| Defines the effect as an interruptible region. This is the default of all effect at creation.<br>Any uninterruptible effect in this region will still be effective.                                                                                            	|
|                       `uninterruptible`                      	|               	| Defines the effect as an uninterruptible region.<br>Any interruptible effect in this region will still be effective.                                                                                                                                           	|
|                         `disconnect`                         	|               	| Allow the interruption effect (finalizer and anything else) to be run asynchronously for this effect.<br>Practical if the liberation of resource is time consuming.<br>Practical if the effect is composed with other effect whose finalizer we have to wait.  	|
| Compose effects in parallel                                  	|               	|                                                                                                                                                                                                                                                                	|
| _combinator_                                                 	| _synonym_     	| _definition_                                                                                                                                                                                                                                                   	|
| Compose effects in parallel                                  	|               	|                                                                                                                                                                                                                                                                	|
| _combinator_                                                 	| _synonym_     	| _definition_                                                                                                                                                                                                                                                   	|
## References family

## `Promise`

Represent a variable (shareable between `Fiber`s) that: 
- can be set only once
- synchronize between fibers by waiting for the promise to be fulfilled

|              Type             	| **Promise[E,A]**                                                                                                                                                                                                                                                   	|
|:-----------------------------:	|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------	|
|           _Creation_          	|                                                                                                                                                                                                                                                                    	|
|          `make[E,A]`          	| Return a unfailing effect of the promise.<br>You create an empty shell for the future value to be set.                                                                                                                                                             	|
| _Waiting/checking completion_ 	|                                                                                                                                                                                                                                                                    	|
|            `await`            	| Return the value of the promise.<br>Block the current fiber for completion of the promise                                                                                                                                                                          	|
|             `poll`            	| Return and optional value of the promise.                                                                                                                                                                                                                          	|
|            `isDone`           	| Return the completion stage of the promise as a boolean                                                                                                                                                                                                            	|
|     _Complete the promise_    	| (Method that returns a boolean return `false` <br>when the `Promise` has already been completed and else `true`)                                                                                                                                                   	|
|          `succeed a`          	| Return a boolean. Complete the promise with a value.                                                                                                                                                                                                               	|
|          `complete e`         	| Return a boolean. <br>Complete the promise with **1** effect that will be executed **one** time and memoized.<br>All awaiting fibers will receive the same value.                                                                                                  	|
|        `completeWith e`       	| Return a boolean.Complete the promise with **1** effect that will be executed for each awaiting fiber.<br>Thus, the result can be different for each fiber.<br>Way faster than `complete`.<br>Return `false` if the promise has already been completed else `true` 	|
|           `done ex`           	| Return a boolean.Complete the promise with an exit value.                                                                                                                                                                                                          	|
|           `fail er`           	| Return a boolean.Complete the promise with the error of type `E`                                                                                                                                                                                                   	|
|           `halt ce`           	| Return a boolean.Complete the promise with a cause typed with `E`                                                                                                                                                                                                  	|
|            `die t`            	| Return a boolean. <br>Interrupt all the awaiting fibers. <br>The current fiber dies with the given `Throwable`                                                                                                                                                     	|
|          `interrupt`          	| Return a boolean. Interrupt all the awaiting fibers.                                                                                                                                                                                                               	|


## The out-of-the-box environment

### Blocking

|         **`Blocking`**         	|                                                                                                                                                               	|
|:------------------------------:	|---------------------------------------------------------------------------------------------------------------------------------------------------------------	|
| `effectBlockingCancelable a e` 	| Create a ZIO effect from a value `a` (the effect to wrap).<br>and a canceler effect `e`. The canceler will be executed on interruption.                       	|
|   `effectBlockingInterrupt a`  	| Create a ZIO effect from a value `a` (the effect to wrap).<br>One interruption, the thread (not the Fiber!) that is running the effect will be interrupted.   	|
