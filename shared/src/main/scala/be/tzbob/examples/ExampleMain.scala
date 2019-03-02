package be.tzbob.examples

import mtfrp.core._
import slogging._

trait ExampleMain extends GavialApp {
  val host = "localhost"
  val port = 8080

  val headExtensions = {
    val nameLC = BuildInfo.name.toLowerCase
    import UI.html.all._
    List(
      link(
        rel := "stylesheet",
        href := "https://cdn.rawgit.com/yegor256/tacit/gh-pages/tacit-css-1.3.2.min.css"),
      script(src := s"$nameLC-fastopt-library.js"),
      raw(
        """
           <script language="JavaScript">
var exports = window;
exports.require = window["ScalaJSBundlerLibrary"].require;
</script>
        """
      ),
      script(src := s"$nameLC-fastopt.js"),
      link(rel := "stylesheet", href := "content/style.css")
    )
  }

}
