package domain

import java.util.UUID

import akka.actor.{ActorRef, Actor, Props, ActorSystem}
import akka.persistence.PersistentActor
import domain.AggregateObject.{Message, Event}

// Global base for all passed messages
object AggregateObject {
  trait Message { val id: String }
  trait Command extends Message
  trait Event extends Message
  trait UniqueId {lazy val id = UUID.randomUUID().toString}
}

// There should be one trait per domain that extends this trait and sets root and all actors extends that trait
trait Aggregate {
  def root: AggregateRootObject
  def recordThat(event: Event) = root recordThat event
}

// Domain object extends this
// All outside communication with specific domain happens through object that extends this
abstract class AggregateRootObject {
  def name: String
  def rootProps: Props
  def streamProps = Props(classOf[Stream], name)

  protected val system = ActorSystem(name)
  protected val domain = actorOf(rootProps)
  protected val stream = actorOf(streamProps)

  def !(message: Message) = ()//domain ! message
  def recordThat(message: Event) = stream ! message
  def actorOf(props: Props) = system actorOf props
}

// Default stream
// Can be overwritten if needed
class Stream(name: String) extends PersistentActor {
  override def persistenceId: String = name

  override def receiveCommand: Receive = {
    case event: Event => persist(event)_
  }

  override def receiveRecover: Receive = {
    case _ => ()
  }
}
