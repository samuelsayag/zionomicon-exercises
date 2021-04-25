The ZIO Cheatsheet
===================

# General considerations

## The language of ZIO
One of the key to master `ZIO` is to understand its language.

The essence of the API is to have a _few number of types_ (as few as possible and very well designed) with a LOT of combinators.
+ The combinators names between the types follow some conventions such as `map`, `flatMap`, `fold`, `zip` etc. 
+ The most commonly encountered combinators have _symbol_ aliases `flatMap` <=> `>>=`, `zip` <=> `<*>` etc.
+ The combinators bridge the type with the standard Scala API whenever possible (`Option`, `Either`, `Future`).  
+ The combinators try to give relations between types of the ZIO library when possible.

## Convention in this sheet
+ Effect of type ZIO are call `e`, `e1`, `e2`... 
+ Collection are taken in a broad sense when functions apply to them. They can be: `Option`, `Set`, `Chunk`, `Array`, `Collection <: Iterable`. This document just use the type `Collection` that do not exist per se but allows to write `Collection[A]` and intends all the concrete types cited above. 

# The `ZIO` type

The `ZIO[-R,+E,+A]` type is central to the whole API and is extremelly rich in combinators.

They have different purpose and can be roughly categorized as follows:
+ Compose effect sequentially
+ Compose effect in parallel
+ Act on the execution of the effect / its channel
+ Act on the error channel
+ Act on the environment channel
+ Provide managed resources 

| Type                        	| ZIO[-R,+E,+A] 	|                                                                                     	|
|-----------------------------	|---------------	|-------------------------------------------------------------------------------------	|
| **Sequential execution**    	|               	|                                                                                     	|
|         _combinator_        	|   _synonym_   	| _definition_                                                                        	|
|         `e1 zip e2`         	|  `e1 <*> e2`  	| Sequentially execute e1 and e2 and combine the result into a tuple                  	|
|       `e1 zipRight e2`      	|  `e1 *> e2`   	| idem but keep e2                                                                    	|
|       `e1 zipLeft e2`       	|   `e1 <* e2`  	| idem but keep e1                                                                    	|
| **Parallel execution**      	|               	|                                                                                     	|
|         _combinator_        	|   _synonym_   	| _definition_                                                                        	|
|        `e1 zipPar e2`       	|  `e1 <&> e2`  	| Parallely execute e1 and e2 and combine the result into a tuple                     	|
|     `e1 zipParRight e2`     	|  `e1 &> e2`   	| idem but keep e2                                                                    	|
|      `e1 zipParLeft e2`     	|   `e1 <& e2`  	| idem but keep e1                                                                    	|
| **Racing execution**        	|               	|                                                                                     	|
|         _combinator_        	|   _synonym_   	| _definition_                                                                        	|
|      `e1 raceEither e2`     	|  `e1 <\|> e2` 	| return the first to succeed as Either[A,B]                                          	|
|         `e1 race e2`        	|               	| return the first to succeed if they are of the same type                            	|
|       `e1 raceFist e2`      	|               	| return the first to finish (whatever result E/A, if E all the Cause[E] are present) 	|
|         `raceAll c`         	|               	| return the first effect to succeed when given a collection of effect.               	|
| Compose effects in parallel 	|               	|                                                                                     	|
|         _combinator_        	|   _synonym_   	| _definition_                                                                        	|
| Compose effects in parallel 	|               	|                                                                                     	|
|         _combinator_        	|   _synonym_   	| _definition_                                                                        	|
| Compose effects in parallel 	|               	|                                                                                     	|
|         _combinator_        	|   _synonym_   	| _definition_                                                                        	|
| Compose effects in parallel 	|               	|                                                                                     	|
|         _combinator_        	|   _synonym_   	| _definition_                                                                        	|
