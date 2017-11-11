package onextent.akka.persistence.demo.models.assessments

import onextent.akka.persistence.demo.models.JsonSupport
import spray.json._

trait AssessmentJsonSupport extends JsonSupport {

  implicit val assessmentFormat: RootJsonFormat[Assessment] = jsonFormat4(
    Assessment)
}
