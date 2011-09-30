package me.winsh

import sbt._
import sbt.CommandSupport._

object SbtScalaEditPlugin extends Plugin {

  //override lazy val settings = Seq(Keys.commands += shCommand)

  def editCommand = Command.args("scalaedit", "[File list...]") { (state, args) => 
    val ret = args.mkString(" ") !
    
    state
  }

}