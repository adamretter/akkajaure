package uk.org.adamretter.akkajaure

trait Pool {
  type LeaseValueProducer[V] = { def value: V }
  type LeaseProducer[V] = { def leased: LeaseValueProducer[V] }
}
