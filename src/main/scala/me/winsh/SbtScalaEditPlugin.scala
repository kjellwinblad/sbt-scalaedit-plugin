package me.winsh

import sbt._
import sbt.CommandSupport._

object SbtScalaEditPlugin extends Plugin {

  override lazy val settings = Seq(Keys.commands += scalaEditCommand)

  def scalaEditCommand = Command.args("scalaedit", "[File list...]") { (state, args) => 
    val ret = args.mkString(" ") !
    println("Hejsan")
    state
  }

}