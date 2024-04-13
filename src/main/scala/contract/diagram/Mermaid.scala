package contract.diagram

import contract.given

@main def generateMermaidDiagram(): Unit = {

  val script = s"""cat > diagram.md << 'EOF'
                  |# Negotiate Draft Contract
                  |```mermaid
                  |${CreateDraftAsMermaid()}
                  |```
                  |
                  |# Enact Contract
                  |```mermaid
                  |${EnactContractAsMermaid()}
                  |```
                  |EOF
                  |
                  |docker run --rm -v "$$PWD:/data" minlag/mermaid-cli -i /data/diagram.md -o /data/diagram.svg
                  |""".stripMargin
  println(script)
}

