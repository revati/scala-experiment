package controllers

import akka.actor.Props
import blog.BlogList.LastEntries
import blog.{BlogEntry, BlogList, BlogView, Blog}
import play.api.mvc._

class Application extends Controller {

  def index = Action {
    val blogList = Blog.actorOf(Props[BlogList])
    val blogEntry = Blog.actorOf(Props(classOf[BlogEntry], "ee9b5721-90ab-4ffb-b8ed-24a0e9c7a622"))
    Blog ! Blog.CreateNewEntry()
    blogList ! LastEntries()
    Ok(views.html.index("Your new application is ready."))
  }
}
