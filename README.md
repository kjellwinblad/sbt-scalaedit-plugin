sbt-scalaedit-plugin
====================

sbt plugin to make it easy to start [ScalaEdit](http://code.google.com/p/scala-edit/) from sbt and to upgrade to the latest version.

Installation
------------

The plugin is tested with [sbt 0.11](https://github.com/harrah/xsbt).

Create the file ~/.sbt/plugins/project/Build.scala if it does not exist and place the following text in the file:

	import sbt._
	
	object MyPlugins extends Build {
	  lazy val root = Project("root", file(".")) dependsOn(
	    uri("git://github.com/kjellwinblad/sbt-scalaedit-plugin.git")
	  )
	}


Usage
-----
The plugin provides two new sbt commands:

	scalaedit

To start ScalaEdit and download it if necessary.

	scalaedit-upgrade

To upgrade ScalaEdit to the latest version.