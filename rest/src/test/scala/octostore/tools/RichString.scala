package octostore.tools

object RichString {

  import scala.language.implicitConversions

  implicit def createRichString(string: String): RichString = {
    new RichString(string)
  }
}

class RichString(string: String) {

  def stripWhitespaces: String = {
    string.stripMargin.replaceAll("\\s+", "")
  }
}