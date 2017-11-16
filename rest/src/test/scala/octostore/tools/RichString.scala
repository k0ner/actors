package octostore.tools

import scala.language.implicitConversions

object RichString {

  implicit def create(string: String): RichString = {
    new RichString(string)
  }
}

class RichString(string: String) {

  def stripWhitespaces: String = {
    string.stripMargin.replaceAll("\\s+", "")
  }
}