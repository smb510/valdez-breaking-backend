package models

import java.util.Date

/**
 * Created by scottie on 4/13/15.
 */

case class Story(id: Long,
                  eventType: String,
                  eventBody: String,
                  importDate: Date,
                  isBroadcast: Boolean)


object Story {
  import anorm.SQL
  import anorm.SqlQuery
  import play.api.Play.current
  import play.api.db.DB

  val sql: SqlQuery = SQL("SELECT * FROM stories ORDER BY importDate DESC")


  def getAll: List[Story] = DB.withConnection {
    implicit conn =>
      sql().map(row =>
        Story(row[Long]("id"), row[String]("eventType"), row[String]("eventBody"), row[Date]("importDate"), row[Boolean]("isBroadcast"))
      ).toList
  }

  val mostRecentUnbroadcast = SQL("SELECT * FROM stories WHERE isBroadcast=FALSE ORDER BY importDate LIMIT 1")

  def getMostRecentUnbroadcast: Story = DB.withConnection {
    implicit conn =>
      sql().map(row =>
        Story(row[Long]("id"), row[String]("eventType"), row[String]("eventBody"), row[Date]("importDate"), row[Boolean]("isBroadcast"))
      ).toList.head
  }

  def insert(story: Story): Boolean = {
    DB.withConnection { implicit connection =>
      SQL( """insert
      into stories
      values (NULL, {eventType}, {eventBody}, {importDate}, {isBroadcast})""").on(
          "eventType" -> story.eventType,
          "eventBody" -> story.eventBody,
          "importDate" -> story.importDate,
          "isBroadcast" -> story.isBroadcast
        ).executeUpdate() == 1
    }
  }

    def bulkInsert(stories: List[Story]): Boolean = {
      stories.map( story =>
      insert(story)).foldLeft(true) {
        (start, acc) => start && acc
      }
    }


  }