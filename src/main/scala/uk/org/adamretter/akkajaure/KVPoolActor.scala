package uk.org.adamretter.akkajaure

trait KVPoolActor {

  /**
   *  A key -> value that have been Leased from the Pool
   */
  type Leased[K, V] = (K, V)

  //Sent messages
  sealed trait SentMessage
  case class Lease[K, V](val leased: Leased[K, V]) extends SentMessage
  case class PoolError(message: String) extends SentMessage

  //Received Messages
  sealed trait RecvMessage
  case class Borrow[K](key: K) extends RecvMessage
  case class Return[K, V](leased: Leased[K, V]) extends RecvMessage
}
