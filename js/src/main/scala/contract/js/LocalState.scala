package contract.js

import org.scalajs.dom
import scala.util.control.NonFatal

/** Contains stuff we need for the UI
  */
object LocalState {

  private enum Keys:
    case CurrentTab, Scenarios
    def name = toString

  private def get(key: String): Option[String] = Option(dom.window.localStorage.getItem(key))
  private def set(key: String, value: String)  = dom.window.localStorage.setItem(key, value)

  def currentTab: ActiveTab =
    get(Keys.CurrentTab.name).map(ActiveTab.valueOf).getOrElse(ActiveTab.Flow)
  def currentTab_=(tab: ActiveTab): Unit = set(Keys.CurrentTab.name, tab.toString())

  def scenariosByName: Map[String, TestScenario] =
    try {
      get(Keys.Scenarios.name)
        .map(s => ujson.read(s))
        .map(TestScenario.mapFromJson)
        .getOrElse(Map.empty)
    } catch {
      case NonFatal(e) =>
        println(s"Error reading scenarios from local storage: $e")
        Map.empty
    }

  def addScenario(scenario: TestScenario): Map[String, TestScenario] = {
    val newMap = scenariosByName.updated(scenario.name, scenario)
    val jason  = ujson.write(newMap.view.mapValues(_.asJson).toMap)
    println("SAVING:")
    println(jason)
    set(Keys.Scenarios.name, jason)
    println(s"saved: ${newMap.mkString("\n", "\n", "\n")}")
    newMap
  }

}
