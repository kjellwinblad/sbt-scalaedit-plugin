sbtPlugin := true

name := "sbt-scalaedit-plugin"

organization := "me.winsh"

version := "2.8.1.final"

scalacOptions ++= Seq("-deprecation")

seq(com.typesafe.sbtscalariform.ScalariformPlugin.defaultSettings: _*)