The language of ZIO
===================

Concept:
* act on the execution of the effect
* compose effect sequentially
* compose effect in parallel
* act on the success channel
* act on the error channel
* act on environment channel
* resource management

| Type                        	| ZIO          	|                                                                                     	|
|-----------------------------	|--------------	|-------------------------------------------------------------------------------------	|
| Sequential execution        	|              	|                                                                                     	|
|          combinator         	|    synonym   	| definition                                                                          	|
|         `e1 zip e2`         	|  `e1 <*> e2` 	| Sequentially execute e1 and e2 and combine the result into a tuple                  	|
|       `e1 zipRight e2`      	|  `e1 *> e2`  	| idem but keep e2                                                                    	|
|       `e1 zipLeft e2`       	|  `e1 <* e2`  	| idem but keep e1                                                                    	|
| Parallel execution          	|              	|                                                                                     	|
|          combinator         	|    synonym   	| definition                                                                          	|
|        `e1 zipPar e2`       	|  `e1 <&> e2` 	| Parallely execute e1 and e2 and combine the result into a tuple                     	|
|     `e1 zipParRight e2`     	|  `e1 &> e2`  	| idem but keep e2                                                                    	|
|      `e1 zipParLeft e2`     	|  `e1 <& e2`  	| idem but keep e1                                                                    	|
| Racing execution            	|              	|                                                                                     	|
|          combinator         	|    synonym   	| definition                                                                          	|
|      `e1 raceEither e2`     	| `e1 <\|> e2` 	| return the first to succeed as Either[A,B]                                          	|
|         `e1 race e2`        	|              	| return the first to succeed if they are of the same type                            	|
|       `e1 raceFist e2`      	|              	| return the first to finish (whatever result E/A, if E all the Cause[E] are present) 	|
|    `raceAll Iterable[e]`    	|              	| return the first to succeed                                                         	|
| Compose effects in parallel 	|              	|                                                                                     	|
| combinator                  	| synonym      	| definition                                                                          	|
| Compose effects in parallel 	|              	|                                                                                     	|
| combinator                  	| synonym      	| definition                                                                          	|
| Compose effects in parallel 	|              	|                                                                                     	|
| combinator                  	| synonym      	| definition                                                                          	|
| Compose effects in parallel 	|              	|                                                                                     	|
| combinator                  	| synonym      	| definition                                                                          	|
