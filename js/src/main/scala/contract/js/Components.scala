package contract.js

enum Components(val name: String, val function: String):
  case ScenarioBuilder extends Components("🏗️ Scenario Builder", "createScenarioBuilder")
  case SequenceDiagram extends Components("⮂ Sequence Diagram", "createSequenceDiagram")
  case Interactive     extends Components("▷ Interactive", "createInteractivePage")
  case Diff            extends Components("Δ Diff", "createDiffPage")

object Components {

  def byFunction(f: String): Option[Components] = values.find(_.function == f)

  private var active = Set[Components]()

  EventBus.tabOpen.subscribe { c =>
    active += c
    println("opened " + c + ", " + active.mkString(","))
    EventBus.activeTabs.publish(active)
  }

  EventBus.tabClosed.subscribe { c =>
    active -= c
    println("closed " + c + ", " + active.mkString(","))
    EventBus.activeTabs.publish(active)
  }
}
