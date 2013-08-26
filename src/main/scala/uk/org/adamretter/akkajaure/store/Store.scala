package uk.org.adamretter.akkajaure.store

import scala.collection.immutable.Queue

trait Store[V] {

  def + (item: Item[V]) : Store[V]

  def unapplySeq[B <: Store[V]](item: B) : Option[B] = Some(item)

//  def head: Item[V]
//  def tail: Store[V]
//  def ::(elem: Item[V]) : QueueStore[V]
}

trait StoreFactory[V] {
  def empty: Store[V]
  def apply(value: Item[V]*) : Store[V]
}
