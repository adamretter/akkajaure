package uk.org.adamretter.akkajaure

trait Item[V] {
  val value: V
}

trait LifeTime[V] {
  item: Item[V] =>

  type ExpiredFn = (V, Long) => Boolean

  val created = System.currentTimeMillis()
  val expired: ExpiredFn
}

case class ImmortalItem[V](override val value: V) extends Item[V]
case class MortalItem[V](override val value: V, override val expired: LifeTime[V]#ExpiredFn) extends Item[V] with LifeTime[V]
