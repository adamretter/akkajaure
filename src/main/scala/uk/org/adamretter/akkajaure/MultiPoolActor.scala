package uk.org.adamretter.akkajaure

import akka.actor.Actor
import akka.actor.FSM
import scala.collection.immutable.Queue

//object MultiPoolActor extends KVPoolActor {
//
//  //internal states
//  sealed trait PoolState
//  case object Uninitialized extends PoolState
//  case object Pooling extends PoolState
//
//  //internal data state
//  type ItemQueue[V] = Queue[_ <: Item[V]]
//  type MappedItemQueue[K, V] = Map[K, ItemQueue[V]]
//  type Pooled[K, V] = MappedItemQueue[K, V]
//  type Borrowed[K, V] = MappedItemQueue[K, V]
//  type Pool[K, V] = (Borrowed[K, V], Pooled[K, V])
//
//  trait MultiPoolActor[K, V] extends Actor with FSM[PoolState, Pool[K, V]] {
//
//    val EmptyMappedItemQueue = Map.empty[K, ItemQueue[V]]
//    val EmptyPool = (EmptyMappedItemQueue, EmptyMappedItemQueue)
//
//    def createItem(key: K): Item[V]
//
//
//    //initial state
//    startWith(Uninitialized, EmptyPool)
//
//    when(Uninitialized) {
//      case Event(borrowMsg: Borrow[K], EmptyPool) =>
//        //create new item and lease it
//        val item = createItem(borrowMsg.key)
//        goto(Pooling) using((queue(borrowMsg.key, item), EmptyMappedItemQueue)) replying Lease((borrowMsg.key, item.value))
//
//      case Event(returnMsg: Return[K, V], EmptyPool) =>
//        //cannot return an item which was not leased from our pool
//        sender ! PoolError(s"${returnMsg.leased} was not leased from this pool!")
//        stay
//    }
//
//    when(Pooling) {
//      case Event(borrowMsg: Borrow[K], (borrowed, pooled)) =>
//        val (maybeItem, keyPooled) = extractFirstActiveEvictExpired(pooled(borrowMsg.key))
//        val item = maybeItem.getOrElse(createItem(borrowMsg.key))
//        val nowBorrowed = queue(borrowMsg.key, item, borrowed)
//        val nowPooled = pooled + (borrowMsg.key -> keyPooled)
//        stay using((nowBorrowed, nowPooled)) replying Lease((borrowMsg.key, item.value))
//
//      case Event(returnMsg: Return[K, V], (borrowed, pooled)) =>
//        val (key, value) = (returnMsg.leased._1, returnMsg.leased._2)
//        val (maybeItem, keyBorrowed) = extractFirstActiveEvictExpired(borrowed(key))
//        maybeItem match {
//          case Some(item) if item.value == value =>
//              //return the item to the pool
//              val nowPooled = queue(key, item, pooled)
//              val nowBorrowed = borrowed + (key -> keyBorrowed)
//              stay using((nowBorrowed, nowPooled))
//          case _ =>
//            //cannot return an item which was not leased from our pool
//            sender ! PoolError(s"${returnMsg.leased} was not leased from this pool!")
//            stay
//        }
//    }
//
//    initialize()
//
//    private def queue(key: K, item: Item[V], mappedItemQueue: MappedItemQueue[K,V] = Map.empty[K, Queue[Item[V]]]) : MappedItemQueue[K,V]= {
//      mappedItemQueue.get(key) match {
//        case Some(items) =>
//          mappedItemQueue + (key -> (items :+ item))
//        case None =>
//          mappedItemQueue + (key -> Queue(item))
//      }
//    }
//  }
//}
