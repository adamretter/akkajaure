package uk.org.adamretter.akkajaure

import akka.actor.{Props, FSM, Actor, ActorRef}
import uk.gov.tna.dri.pool.PoolActor.PoolActor

object ActorPoolActor extends KVPoolActor {

  //internal states
  sealed trait PoolState
  case object Uninitialized extends PoolState
  case object Pooling extends PoolState

  //internal data state
  type ActorMap[K] = Map[K, ActorRef]
  type Pool[K] = ActorMap[K]

  trait ActorPoolActor[K, V] extends Actor with FSM[PoolState, Pool[K]] {

    val EmptyActorMap = Map.empty[K, ActorRef]
    val EmptyPool = EmptyActorMap

    def createItem(key: K): Item[V]


    //initial state
    startWith(Uninitialized, EmptyPool)

    when(Uninitialized) {
      case Event(borrowMsg : Borrow[K], EmptyPool) =>
        //create an actor and forward it the borrow message
        val subPoolActor = context.actorOf(Props(classOf[SubPoolActor[K, V]], createItem _, borrowMsg.key))
        //transform the borrow msg to the subPoolActor and set the sender to our sender
        subPoolActor.tell(PoolActor.Borrow, sender)
        goto(Pooling) using Map(borrowMsg.key -> subPoolActor)

      case Event(Return(leased), EmptyPool) =>
        //cannot return an item which was not leased from our pool
        sender ! PoolError(s"$leased was not leased from this pool!")
        stay
    }

    when(Pooling) {
      case Event(borrowMsg: Borrow[K], pool) =>
        //get or create an actor and forward it the borrowMsg
        val subPoolActor = pool.getOrElse(borrowMsg.key, context.actorOf(Props(classOf[SubPoolActor[K, V]], createItem _, borrowMsg.key)))
        //transform the borrow msg to the subPoolActor and set the sender to our sender
        //subPoolActor.tell(PoolActor.Borrow, sender)
        subPoolActor.tell(PoolActor.Borrow, sender)
        val nowPool = if(pool.contains(borrowMsg.key)) pool else pool + (borrowMsg.key -> subPoolActor)
        stay using nowPool

      case Event(returnMsg: Return[K, V], pool) =>
        pool.get(returnMsg.leased._1) match {
          case Some(subPoolActor) =>
            //transform the return msg to the subPoolActor and set the sender to our sender
            subPoolActor.tell(PoolActor.Return[V](returnMsg.leased._2), sender)
          case None =>
            //no sub-actor, so cannot return an item which was not leased from our pool
            sender ! PoolError(s"${returnMsg.leased} was not leased from this pool!")
        }
        stay
    }

    initialize()
  }

  class SubPoolActor[K, V](val storeFactory: StoreFactory[V], val itemFactory: (K) => Item[V], val key: K) extends PoolActor[V] {
    def createItem() = itemFactory(key)
  }
}
