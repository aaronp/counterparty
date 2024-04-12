run: package
	 java -cp ./target/scala-3.3.0/contracts-assembly-0.0.1.jar contract.server.Server

generateMermaidFast:
	 sbt --warn "runMain contract.diagram.generateMermaidDiagram" | tail -n +1 | bash

generateMermaid: package
	 java -cp ./target/scala-3.3.0/contracts-assembly-0.0.1.jar contract.diagram.generateMermaidDiagram

package: packageRestCode
	sbt assembly

packageRestCode: generateRestCode
	cd server && sbt publishLocal

generateRestCode:
	docker run --rm -v ${PWD}:/local openapitools/openapi-generator-cli:latest generate \
        -i /local/service.yaml \
        -g scala-cask \
        -c /local/openapi-config.yaml \
        -o /local/server
