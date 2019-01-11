package be.tzbob.circleroyale

import mtfrp.core.UI.HTML
import mtfrp.core.macros.client
import mtfrp.core._
import slogging.LazyLogging

class SvgFRP(name: String, totalWidth: Int, totalHeight: Int)
    extends LazyLogging {
  private[this] val mouseSource: ClientEventSource[(Double, Double)] =
    ClientEvent.source[(Double, Double)]

  val mousePosition: ClientEvent[(Double, Double)] = mouseSource

  @client private[this] lazy val svgElement = {
    import org.scalajs.dom
    import org.scalajs.dom.raw.SVGSVGElement

    dom.document.getElementById(name).asInstanceOf[SVGSVGElement]
  }

  @client private[this] lazy val svgPoint = svgElement.createSVGPoint()

  def svg(camera: ClientBehavior[Camera],
          svgContent: ClientBehavior[Seq[HTML]]): ClientBehavior[HTML] = {

    svgContent.map2(camera) { (svgElements, cam) =>
      import mtfrp.core.UI.html.all
      import mtfrp.core.UI.html.all._
      import mtfrp.core.UI.html.svgTags
      import mtfrp.core.UI.html.svgAttrs

      logger.debug(s"camera used: ${cam.viewboxText}")

      val svgField = svgTags.svg(
        xmlns := "http://www.w3.org/2000/svg",
        svgAttrs.viewBox := cam.viewboxText,
        style := "border: 1px solid red;",
        all.id := this.name,
        UI.listen(onmousemove, mouseSource) { e =>
          @client val tup = {
            import org.scalajs.dom.raw.MouseEvent
            val mouseE = e.asInstanceOf[MouseEvent]
            svgPoint.x = mouseE.clientX
            svgPoint.y = mouseE.clientY
            val p =
              svgPoint.matrixTransform(svgElement.getScreenCTM().inverse())
            p.x -> p.y
          }
          tup
        }
      )(
        svgTags.defs(
          svgTags.pattern(
            id := "bg",
            svgAttrs.patternUnits := "userSpaceOnUse",
            svgAttrs.x := "0",
            svgAttrs.y := "0",
            svgAttrs.width := "100",
            svgAttrs.height := "100",
            svgTags.image(
              attr("xlink:href") := "content/topography.svg",
              svgAttrs.x := "0",
              svgAttrs.y := "0",
              svgAttrs.width := "100",
              svgAttrs.height := "100"
            )
          )
        ),
        svgTags.rect(
          `class` := "playingField",
          svgAttrs.x := 0,
          svgAttrs.y := 0,
          svgAttrs.width := totalWidth,
          svgAttrs.height := totalHeight,
          svgAttrs.fill := "url(#bg)",
          svgAttrs.fillOpacity := "0.2",
          svgAttrs.stroke := "black",
          svgAttrs.strokeWidth := "5",
          svgAttrs.strokeLinecap := "round"
        )
      )(
        svgElements
      )

      div(svgField)
    }
  }
}
