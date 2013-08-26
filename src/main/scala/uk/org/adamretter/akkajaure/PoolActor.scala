package uk.org.adamretter.akkajaure

import akka.actor.Actor
import akka.actor.FSM

object PoolActor extends Pool {

   /**
    *  A value that have been Leased from the Pool
    */
   type Leased[V] = V

   //internal states
   sealed trait PoolState
   case object Uninitialized extends PoolState
   case object Pooling extends PoolState

   //internal data state
   type Pooled[V] = Store[V]
   type Borrowed[V] = Store[V]
   type Pool[V] = (Borrowed[V], Pooled[V])

   //Sent messages
   sealed trait SentMessage
   case class Lease[V](leased: Leased[V]) extends SentMessage
   case class PoolError(message: String) extends SentMessage

    //Received Messages
   sealed trait RecvMessage
   object Borrow extends RecvMessage
   case class Return[V](leased: Leased[V]) extends RecvMessage

   trait PoolActor[V] extends Actor with FSM[PoolState, Pool[V]] {

     //storage for the pool
     val storeFactory: StoreFactory[V]

     //factory method to create a new Item for the pool
     def createItem(): Item[V]

     def EmptyStore = storeFactory.empty
     val EmptyPool = (EmptyStore, EmptyStore)

     //initial state
     startWith(Uninitialized, EmptyPool)

     when(Uninitialized) {
       case Event(Borrow, EmptyPool) =>
         //create new item and lease it
         val item = createItem()
         goto(Pooling) using((storeFactory(item), EmptyStore)) replying Lease(item.value)

       case Event(Return(leased), EmptyPool) =>
         //cannot return an item which was not leased from our pool
         sender ! PoolError(s"$leased was not leased from this pool!")
         stay
     }

     when(Pooling) {
       case Event(Borrow, (borrowed, pooled)) =>
         val (maybeItem, maybeNowPooled) = extractFirstActiveEvictExpired(pooled)
         val nowPooled = maybeNowPooled.getOrElse(storeFactory.empty)
         val item = maybeItem.getOrElse(createItem())
         stay using((borrowed + item, nowPooled)) replying Lease(item.value)

       case Event(returnMsg: Return[V], (borrowed, pooled)) =>
         val (maybeItem, maybeNowBorrowed) = matchFirstActiveEvictExpired(borrowed, returnMsg.leased)
         val nowBorrowed = maybeNowBorrowed.getOrElse(storeFactory.empty)
         maybeItem match {
           case Some(item) =>
               //return the item to the pool
               stay using((nowBorrowed, pooled + item))
           case _ =>
             //cannot return an item which was not leased from our pool
             sender ! PoolError(s"${returnMsg.leased} was not leased from this pool!")
             stay
         }
     }

     initialize()
   }
 }
