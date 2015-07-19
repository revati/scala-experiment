package blog

import java.util.UUID

import akka.actor.{Actor, Props, ActorSystem}
import akka.persistence.{PersistentView, PersistentActor}
import blog.Blog.{NewEntryCreated, CreateNewEntry, Command, Event}
import blog.BlogList.{LastEntries}


object Blog {
  def props: Props = Props[Blog]

  val system = ActorSystem("blog")
  val domain = system.actorOf(props)
  val stream = system.actorOf(Props[BlogStream])

  trait Command {val id: String}
  trait UniqueId {lazy val id = UUID.randomUUID().toString}
  trait Event {val id: String}

  case class CreateNewEntry() extends Command with UniqueId
  case class NewEntryCreated(id: String) extends Event
}

class BlogStream extends PersistentActor {
  override def persistenceId: String = "blog"

  override def receiveCommand: Receive = {
    case event: Event => persist(event)(broadcast)
    case command: Command => broadcast(command)
  }

  def broadcast(x: Any) = println(x.toString)

  override def receiveRecover: Receive = {
    case _ => ()
  }
}

class Blog extends Actor {
  override def receive: Receive = {
    case command: CreateNewEntry => recordThat(NewEntryCreated(command.id))
  }

  def recordThat(event: Event) = Blog.stream ! event
}

class BlogView extends PersistentView {
  override def viewId: String = "blog-view"
  override def persistenceId: String = "blog"

  override def receive: Actor.Receive = {
    case x: Event => println( "view::" + x.toString)
  }
}

object BlogList {
  case class LastEntries(limit: Int = 10)
}

class BlogList extends PersistentView {
  protected case class Entry(id: String)

  protected var entries: List[Entry] = List()

  override def viewId: String = "blog-view"
  override def persistenceId: String = "blog"

  override def receive: Actor.Receive = {
    case request: LastEntries => println(entries.toString)
    case event: NewEntryCreated => entries = Entry(event.id) :: entries
  }
}

class BlogEntry(id: String) extends PersistentView {
  override def viewId: String = "blog-view"
  override def persistenceId: String = "blog"

  override def receive: Actor.Receive = {
    case event: NewEntryCreated if event.id == id => println("entry::" + event.toString)
  }
}