package free.examples.etl.ui

import java.time.*

case class InMemoryDB() {
//  private val timestampByUserId = collection.mutable.Map[UserId, ZonedDateTime]()

//  def update(user: UserId, now: ZonedDateTime): Unit = {
  def update(user: Any, now: ZonedDateTime): Unit = {
//    timestampByUserId.put(user, now)
  }

//  def get(user: UserId): Option[ZonedDateTime] = timestampByUserId.get(user)
  def get(user: Any): Option[ZonedDateTime] = None
}
