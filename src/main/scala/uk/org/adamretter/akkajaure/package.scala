package uk.org.adamretter.akkajaure

import scala.annotation.tailrec

package object pool {

  /**
   * Extracts the first Item from a queue which has not expired
   *
   * Whilst looking for the first live Item, if any expired Items
   * are encountered, they will be evicted from the Queue
   *
   * @param items A Queue of Items
   *
   * @return _1 The first live Item if found. _2 a copy of items without the live Item
   *         and any expired Items removed that were encountered on the way to the
   *         first Live Item (That is to say that further expired items may
   *         still exist in the queue)
   */
  @tailrec
  def extractFirstActiveEvictExpired[V](items: Store[V]): (Option[_ <: Item[V]], Option[Store[V]]) = {
    items match {
      case head :: tail =>
        head match {
          case item : ImmortalItem[V] =>
            (Option(item), Option(tail))

          case item: MortalItem[V] if !item.expired(item.value, item.created) =>
            (Option(item), Option(tail))

          case item: MortalItem[V] if item.expired(item.value, item.created) =>
            extractFirstActiveEvictExpired(tail)
        }

      case nil =>
        (None, None)
    }
  }

  @tailrec
  def matchFirstActiveEvictExpired[V](items: Store[V], against: V): (Option[_ <: Item[V]], Option[Store[V]]) = {

    items match {
      case head +: tail =>
        head match {
          case item : ImmortalItem[V] if(item.value == against) =>
            (Option(item), Option(tail))

          case item: MortalItem[V] if !item.expired(item.value, item.created) && item.value == against =>
            (Option(item), Option(tail))

          case item: MortalItem[V] if !item.expired(item.value, item.created) && item.value != against =>
            matchFirstActiveEvictExpired(tail, against)

          case item: MortalItem[V] if item.expired(item.value, item.created) =>
            matchFirstActiveEvictExpired(tail, against)
        }

      case nil =>
        (None, None)
    }
  }
}
