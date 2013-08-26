package uk.org.adamretter.akkajaure.store

import scala.collection.immutable.{List, Nil, Queue}
import scala.collection.generic.CanBuildFrom

class QueueStore[A](in: List[Item[A]], out: List[Item[A]]) extends Queue[Item[A]](in, out) with Store[A] {

  def +(elem: Item[A]) = new QueueStore[A](elem :: in, out)

  def unapplySeq(item: Item[A]) = ???

//  def ::(elem: Item[A]) = new QueueStore[A](elem :: in, out)

//  override def tail = {
//    if(out.nonEmpty)
//      new QueueStore(in, out.tail)
//    else if(in.nonEmpty)
//      new QueueStore(Nil, in.reverse.tail)
//    else
//      throw new NoSuchElementException("tail on empty queue")
//  }

}

class QueueStoreFactory[A] extends StoreFactory[A] {
  def empty = EmptyQueueStore.asInstanceOf[QueueStore[A]]
  def apply(values: Item[A]*) = new QueueStore[A](Nil, values.toList)

  private object EmptyQueueStore extends QueueStore[Nothing](Nil, Nil) { }
}
