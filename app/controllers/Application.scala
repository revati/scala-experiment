package controllers

import akka.actor.Props
import blog.BlogList.LastEntries
import blog.{BlogEntry, BlogList, Blog}
import play.api.mvc._

class Application extends Controller {

  def index = Action {
    val blogList = Blog.actorOf(Props[BlogList])
    val blogEntry = Blog.actorOf(Props(classOf[BlogEntry], "b7d7ed6c-2704-4cc1-bacb-9f0cb1077141"))
    Blog ! Blog.CreateNewEntry()
    blogList ! LastEntries()
    Ok(views.html.index("Your new application is ready."))
  }
}
