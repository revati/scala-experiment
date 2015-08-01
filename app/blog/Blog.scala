package blog

import akka.actor.{Actor, Props}
import akka.persistence.PersistentView
import blog.Blog.{NewEntryCreated, CreateNewEntry}
import blog.BlogList.LastEntries
import domain.AggregateObject.{Event, UniqueId, Command}
import domain._

object Blog extends domain.AggregateRootObject {
  def name = "blog"
  def rootProps: Props = Props[Blog]

  case class CreateNewEntry() extends Command with UniqueId
  case class NewEntryCreated(id: String) extends Event

}

trait BlogAggregate extends Aggregate {
  override def root = Blog
}

class Blog extends Actor with BlogAggregate {
  override def receive: Receive = {
    case command: CreateNewEntry => recordThat(NewEntryCreated(command.id))
  }
}

class BlogView extends PersistentView with BlogAggregate {
  override def viewId: String = "blog-view"
  override def persistenceId: String = "blog"

  override def receive: Actor.Receive = {
    case x: Event => println( "view::" + x.toString)
  }
}

object BlogList {
  case class LastEntries(limit: Int = 10)
}

class BlogList extends PersistentView with BlogAggregate {
  protected case class Entry(id: String)

  protected var entries: List[Entry] = List()

  override def viewId: String = "blog-view"
  override def persistenceId: String = "blog"

  override def receive: Actor.Receive = {
    case request: LastEntries => println(entries.toString)
    case event: NewEntryCreated => entries = Entry(event.id) :: entries
  }
}

class BlogEntry(id: String) extends PersistentView with BlogAggregate {
  override def viewId: String = "blog-view"
  override def persistenceId: String = "blog"

  override def receive: Actor.Receive = {
    case event: NewEntryCreated if event.id == id => println("entry::" + event.toString)
  }
}