The ZIO Cheatsheet
===================

# General considerations

## The language of ZIO
One of the key to master `ZIO` is to understand its language.

The essence of the API is to have a _few number of types_ (as few as possible and very well designed) with a LOT of combinators.
+ The combinators names between the types follow some conventions such as `map`, `flatMap`, `fold`, `zip` etc. 
+ The most commonly encountered combinators have _symbol_ aliases `flatMap` <=> `>>=`, `zip` <=> `<*>` etc.
+ when the combinator finish by `M` (ex: `fold` and `foldM`) it signals that the parameter is either and effect (`M` for Monad) or producing and effect and will `flatten` it.
+ The combinators bridge the type with the standard Scala API whenever possible (`Option`, `Either`, `Future`).  
+ The combinators try to give relations between types of the ZIO library when possible.

## Convention in this sheet

You will notice that this cheatsheet gives almost no information about type for a given combinator.

Indeed it leverage the fact that ZIO is written above a very small well designed number of types (other like function are not specific to ZIO of course) that compose incredibly well.

Consequently, the below notation is enumerating these essential types together with a short way of denoting them.

It reads more easily and allows to throw away some complexity of the signature that practice of the library will anyway eliminate.

+ Effect of type ZIO are call `e`, `e1`, `e2`... 
+ Collection, noted `c` are taken in a broad sense when functions apply to them. They can be: `Option`, `Set`, `Chunk`, `Array`, `Collection <: Iterable`. This document just use the type `Collection` that do not exist per se but allows to write `Collection[A]` and intends all the concrete types cited above. 
+ Collection of effect (in the same sense than above) of effect are noted `ce`
+ function is noted `f` (A => B), `fm` (funtion the return effect: A => ZIO[...])
+ zio.duration is noted `d`
+ ZSchedule is noted `s`
+ ZScope is noted `zs`
+ ZManaged is noted `zm`
+ ExecutionContext (scala) `ec`, Executor `ex`

# The `ZIO` type

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

| Type                                                   	| ZIO[-R,+E,+A] 	|                                                                                                          	|
|--------------------------------------------------------	|---------------	|----------------------------------------------------------------------------------------------------------	|
| **Interact with time**                                 	|               	|                                                                                                          	|
|                      _combinator_                      	|   _synonym_   	| _definition_                                                                                             	|
|                         `never`                        	|               	| equivalent to never ending loop but without resource consumption                                         	|
|                        `forever`                       	|               	| execute the effect forever                                                                               	|
|                         `once`                         	|               	| will execute this effect once even if evaluated multiple times                                           	|
|                      `eventually`                      	|               	| repeat the effect until it fails                                                                         	|
|  `retry s` <br/> `retryUntil...` <br/> `retryWhile...` 	|               	| A family of combinator whose essence is to retry the effect with some additional conditions/parameters   	|
|                        `delay d`                       	|               	| delay the effect with a given duration                                                                   	|
| `repeat` <br/> `repeatUntil...` <br/> `repeatWhile...` 	|               	| A family of combinators whose essence is to repeat the effect with some additional conditions/parameters 	|
|          `schedule s` <br/> `scheduleFrom v s`         	|               	| Run the effect                                                                                           	|
| **Sequential execution**                               	|               	|                                                                                                          	|
|                      _combinator_                      	|   _synonym_   	| _definition_                                                                                             	|
|                     `e1 flatMap e2`                    	|  `e1 >>= e2`  	| execute e1 pass it to e2 and form a new effect with result                                               	|
|                       `e1 zip e2`                      	|  `e1 <*> e2`  	| Sequentially execute e1 and e2 and combine the result into a tuple                                       	|
|                    `e1 zipRight e2`                    	|   `e1 *> e2`  	| idem but keep e2                                                                                         	|
|                     `e1 zipLeft e2`                    	|   `e1 <* e2`  	| idem but keep e1                                                                                         	|
|                      `foreach c f`                     	|               	| apply f to the value of c and wrapped the collection in an effect                                        	|
|                     `collectAll ce`                    	|               	| transform a collection of effect to an effect of collection                                              	|
| **Parallel execution**                                 	|               	|                                                                                                          	|
|                      _combinator_                      	|   _synonym_   	| _definition_                                                                                             	|
|                     `e1 zipPar e2`                     	|  `e1 <&> e2`  	| Parallely execute e1 and e2 and combine the result into a tuple                                          	|
|                   `e1 zipParRight e2`                  	|   `e1 &> e2`  	| idem but keep e2                                                                                         	|
|                   `e1 zipParLeft e2`                   	|   `e1 <& e2`  	| idem but keep e1                                                                                         	|
|         `foreachPar c f`   `foreachParN n c f`         	|               	| apply f to the value of c and wrapped the collection in an effect. N is the degree of parallelism.       	|
|         `collectAllPar ce`  `collectParN n ce`         	|               	| transform a collection of effect to an effect of collection. N is the degree of parallelism.             	|
| **Racing execution**                                   	|               	|                                                                                                          	|
|                      _combinator_                      	|   _synonym_   	| _definition_                                                                                             	|
|                   `e1 raceEither e2`                   	|  `e1 <\|> e2` 	| return the first to succeed as Either[A,B]                                                               	|
|                      `e1 race e2`                      	|               	| return the first to succeed if they are of the same type                                                 	|
|                    `e1 raceFist e2`                    	|               	| return the first to finish (whatever result E/A, if E all the Cause[E] are present)                      	|
|                       `raceAll c`                      	|               	| return the first effect to succeed when given a collection of effect.                                    	|
| **Act of the error channel**                           	|               	|                                                                                                          	|
|                      _combinator_                      	|   _synonym_   	| _definition_                                                                                             	|
|                    `flatMapError e`                    	|               	| flatMap on the error channel                                                                             	|
| **Async API**                                          	|               	|                                                                                                          	|
|                      _combinator_                      	|   _synonym_   	| _definition_                                                                                             	|
|                         `fork`                         	|               	| _fork an effect_ in the local (z)scope                                                                   	|
|                      `forkDaemon`                      	|               	| _fork an effect_ the global scope (daemon behavior)                                                      	|
|                       `forkIn zs`                      	|               	| _fork an effect_ in the given scope                                                                      	|
|                       `forkOn ec`                      	|               	| _fork an effect_ in the specified execution context                                                      	|
|                        `lock ex`                       	|               	| _fork an effect_ in the specified executor`                                                              	|
|                    `forkManaged zm`                    	|               	| _fork and effect_ in the given `ZManaged`                                                                	|
|                         `in zs`                        	|               	| _modify the scope of the effect by_ extending the scope of the effect to the given scope                 	|
|                 `overrideForkScope zs`                 	|               	| _modify the scope of the effect by_ **replacing** the scope of the effect by the given scope             	|
|                    `resetForkScope`                    	|               	| _modify the scope of the effect by_ resetting the effect scope to the scope of the current Fiber         	|
|                       `forkScope`                      	|               	| _return the scope_ that will be used to fork effect in this effect                                       	|
|                     `scopeWith fm`                     	|               	| _will pass to the given callback_ the scope of the current fiber                                         	|
|                   `forkScopeWith fm`                   	|               	| _will pass to the given callback_ the scope that will be used to fork effect in this effect              	|
| Compose effects in parallel                            	|               	|                                                                                                          	|
| _combinator_                                           	| _synonym_     	| _definition_                                                                                             	|
| Compose effects in parallel                            	|               	|                                                                                                          	|
| _combinator_                                           	| _synonym_     	| _definition_                                                                                             	|
