package controllers


import java.util.{Date, Calendar}

import org.h2.engine.Database
import play.api._
import play.api.db.DB
import play.api.libs.json.{Json, Writes, JsValue}
import play.api.libs.ws.WS
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.Play.current

import models.Story
import scala.concurrent.Future
import scala.xml.{NodeSeq, Elem}

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def download = Action {
    Ok(views.html.download())
  }


  def hi = Action {
    Ok("hi")
  }

  def web = Action {
    val stories = Story.getAllSince(-1)
    Ok(views.html.web(stories))
  }

  def scrape = Action.async {

    val simpleFuture : Future[Array[Option[Story]]]  = WS.url("http://kvakradio.com/pages/12542342.php").get().map { response =>
      val startIndex = response.body.indexOf("<font class=\"FeatureGroupFont  Box_132763726_Font\">")
      val endIndex = response.body.indexOf("<tr class=\"Box_132763726_feature2\">")
      response.body.substring(startIndex, endIndex).split("\n").map {
        rawLine => parseLine(rawLine)
      }
    }

    simpleFuture.map { i =>
      val success = Story.bulkInsert(i.toList.flatten)
      if (success) {
        Ok("Stories have been imported!")
      } else {
        InternalServerError("an error occurred")
      }
    }

  }


  def parseLine(rawLine: String) : Option[Story]  = {
    val startStrongIndex = rawLine.indexOf("<strong>")
    val endStrongIndex = rawLine.indexOf("</strong>")
    val startBrIndex = rawLine.indexOf("<br />")
    if (startBrIndex == -1 || startStrongIndex == -1 || endStrongIndex == -1) {
      return None
    }
    val incidentType = normalizeString(rawLine.substring(startStrongIndex + "</strong>".length - 1, endStrongIndex))
    val incidentBody = normalizeString(rawLine.substring(endStrongIndex + "</strong>".length, startBrIndex))
    if (! incidentBody.isEmpty) {
      return Some(new Story(Calendar.getInstance().getTimeInMillis, incidentType, incidentBody, Calendar.getInstance().getTime, false))
    } else {
      return None
    }

  }

  def normalizeString(rawString: String) : String =  {
    rawString.replace("&nbsp;", " ")
    .replace("&rsquo;", "'")
    .replace("&ndash;", "-")
    .replace("</span>", "")

  }

  def getAllStories(last: Long) = Action {
    Logger.debug("Last: $last")
    val stories = Story.getAllSince(last)

    val maxLastImportDate: Long = stories.map { story =>
      story.importDate.getTime
    }.max

    var scraped: Boolean = false

   val now: Long = Calendar.getInstance().getTimeInMillis
    if ((now - maxLastImportDate) >= 1000 * 60 * 60 * 24 * 7) {
      scrape
      scraped = true
    }

    if (scraped) {
      Logger.debug("Scraped messages!!")
    }

    implicit val storyWrites = new Writes[Story] {
      override def writes(story: Story) = Json.obj(
      "id" -> story.id,
      "eventType" -> story.eventType.replace(':', ' '),
      "eventBody" -> story.eventBody,
      "importDate" -> story.importDate,
      "isBroadcast" -> story.isBroadcast
      )
    }
    Ok(Json.toJson(stories))
  }


}
