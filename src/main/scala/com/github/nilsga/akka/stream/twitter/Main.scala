package com.github.nilsga.akka.stream.twitter

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

object Main extends App {

  implicit val system: ActorSystem = ActorSystem("akka-stream-twitter")
  implicit val mat: ActorMaterializer = ActorMaterializer()

  if(args.length != 1) {
    println("Usage: Main <filter>")
    System.exit(1)
  }

  TwitterClient(args(0)).start.runForeach(status => println(status.getText))

}
