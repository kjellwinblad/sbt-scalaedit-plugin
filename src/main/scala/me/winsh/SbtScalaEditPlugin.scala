package me.winsh

import sbt._
import sbt.CommandSupport._
import scala.collection.JavaConversions._
import java.lang.ProcessBuilder
import java.net.URL
import scala.xml._
import java.io.FileOutputStream
import scala.annotation.tailrec
import java.io.File

/**
 * sbt plugin to make it easy to start ScalaEdit from sbt and to
 * upgrade to the latest version.
 */
object SbtScalaEditPlugin extends Plugin {

  override lazy val settings = Seq(Keys.commands ++= List(scalaEditCommand, scalaEditUpgradeCommand))

  private val newVersionFeedURLString = "http://code.google.com/feeds/p/scala-edit/downloads/basic"

  private val remoteFilesURLString = "http://scala-edit.googlecode.com/files/"

  private val versionNumberRegExp = """.*-.*-(\d*)\.(\d*)\.(\d*).*""".r

  private val baseDir = new File(new File(System.getProperty("user.home")), ".scalaedit")

  private def excutableJarPath = {

    //Get candidate jars
    val marchingJars: Seq[File] = (baseDir ** "scalaedit*.jar").get

    //Sort the list to get tne latest version first
    val sortedJars = marchingJars.toList.sortWith((e1, e2) => {

      def versionNumberList(file: File) = versionNumberRegExp.unapplySeq(file.getName) match {
        case None => Nil
        case Some(l) => l.map(_.toInt)
      }

      def versionNumber(l: List[Int], multFactor: Double = 1): Double = l match {
        case Nil => 0.0
        case e :: rest => e * multFactor + versionNumber(rest, multFactor * 0.01)
      }

      val file1VersionNumber = versionNumber(versionNumberList(e1))
      val file2VersionNumber = versionNumber(versionNumberList(e2))

      if (file1VersionNumber >= file2VersionNumber) true else false

    })

    //Return the head of the list
    sortedJars.headOption

  }

  /**
   * Finds the latest version and the URL to the file from the google code page
   */
  private def retriveLatestVersionAndURL() = {

    //Fetch XML feed with version history
    var url = new URL(newVersionFeedURLString)
    val connection = url.openConnection
    val input = connection.getInputStream
    val root = XML.load(input)
    input.close

    //Finds XML elemnt containing file name and version number
    val latestEntry = (root \\ "entry").head
    val webURL = (latestEntry \ "id").text.trim

    val fileName = webURL.substring(webURL.lastIndexOf("/"))

    //Finds version from file name
    val versionNumberRegExp(v1, v2, v3) = fileName
    val version = v1 + "." + v2 + "." + v3

    (version, fileName)
  }

  /**
   * Returns the Some(availableVersion) or None
   */
  private def currentVersion = excutableJarPath match {
    case None => None
    case Some(path) => versionNumberRegExp.unapplySeq(path.getName) match {
      case None => None
      case Some(l) => Some(l.mkString("."))
    }
  }

  /**
   * Returns the latest version on googlecode
   */
  private def latestVersion = retriveLatestVersionAndURL()._1

  /**
   * Downloads the latest jar file from googlecode and returns the path to the file
   */
  private def downloadLatestAndGetJarPath() = {

    println("Fetching file information...")

    val (latestVersion, fileName) = retriveLatestVersionAndURL()

    //Download the file
    var url = new URL(remoteFilesURLString + fileName)
    var connection = url.openConnection
    val input = connection.getInputStream
    val fileSize = connection.getContentLength

    println("New version of ScalaEdit (" + latestVersion + ") will be downloaded...")
    println("File size: " + fileSize / 1000000.0 + " MB")
    println("|--------------------|")
    print("|")
    val downloadedFile = baseDir / fileName
    baseDir.mkdirs()
    val output = new FileOutputStream(downloadedFile)

    @tailrec
    def downloadScalaEdit(nrOfBytesDownloaded: Int = 0): Unit = input.read() match {
      case -1 => Unit
      case read => {
        output.write(read)
        if (nrOfBytesDownloaded != 0 && (0 == nrOfBytesDownloaded % (fileSize / 20)))
          print("#")
        downloadScalaEdit(nrOfBytesDownloaded + 1)
      }
    }
    downloadScalaEdit()
    output.close()
    input.close()
    println("|")
    println("Download Succesfull")

    downloadedFile
  }

  def runScalaEdit(jarPath: File) {

    val javaFile = new File(new File(System.getProperty("java.home"), "bin"), "java")

    val javaPath = javaFile.exists match {
      case true => javaFile.getAbsolutePath
      case false => "java"
    }

    val args: java.util.List[String] = javaPath :: "-jar" :: jarPath.getAbsolutePath :: Nil
    val pb = new ProcessBuilder(args)
    pb.directory(file("."))

    pb.start()

  }

  /**
   * Command to start scalaedit
   */
  def scalaEditCommand = Command.command("scalaedit") { (state) =>

    val jarPath = excutableJarPath match {
      case None => downloadLatestAndGetJarPath()
      case Some(file) => file
    }

    runScalaEdit(jarPath)

    state
  }

  /**
   * Command to upgrade ScalaEdit
   */
  def scalaEditUpgradeCommand = Command.command("scalaedit-upgrade") { (state) =>

    if (currentVersion != None && currentVersion.get == latestVersion)
      println("The current version is already the latest version.")
    else {
      downloadLatestAndGetJarPath()
    }

    state
  }

}
