package com.github.nilsga.akka.stream.twitter

import akka.actor.ActorRef
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Keep, Sink, Source}
import twitter4j._

object TwitterClient {

  def apply(filter: String)(implicit mat: ActorMaterializer) =
    new TwitterClient(filter)

}

class TwitterClient(filter: String)(implicit mat: ActorMaterializer) {

  val stream = new TwitterStreamFactory().getInstance()

  def start = {
    val (actorRef, publisher) = Source.actorRef[Status](1000, OverflowStrategy.dropTail).toMat(Sink.asPublisher(false))(Keep.both).run()
    stream.addListener(new FilterStreamListener(actorRef))
    stream.filter(filter)
    Source.fromPublisher(publisher)
  }

}

class FilterStreamListener(ref: ActorRef) extends StatusListener {
  override def onStallWarning(warning: StallWarning): Unit = {
    println(s"Stalling: $warning")
  }

  override def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice): Unit = {
    println(s"Deleted: $statusDeletionNotice")
  }

  override def onScrubGeo(userId: Long, upToStatusId: Long): Unit = {
    println(s"ScrubGeo: $userId, $upToStatusId")
  }

  override def onStatus(status: Status): Unit = {
    ref ! status
  }

  override def onTrackLimitationNotice(numberOfLimitedStatuses: Int): Unit = {
    println(s"TrackLimitation: $numberOfLimitedStatuses")
  }

  override def onException(ex: Exception): Unit = {
    ex.printStackTrace()
  }
}
