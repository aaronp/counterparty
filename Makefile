ARTEFACT_FILE := ./target/scala-3.3.0/contracts-assembly-0.0.1.jar
REST_STUBS_LOCATION := target/rest-stubs
# Check if the dependency file exists
ARTEFACT_EXISTS := $(wildcard $(ARTEFACT_FILE))

ifeq ($(ARTEFACT_EXISTS),)
PACKAGE_DEPENDENCY := package
else
PACKAGE_DEPENDENCY :=
endif

# cleans the whole project (the generated REST service files and these project files)
clean: cleanDiagram cleanRest
	sbt clean

# removes the generated diagram markdown/svg files which get created/derived from the business logic
cleanDiagram:
	find . -name 'diagram*.md' -exec rm {} \;
	find docs -name 'diagram*.svg' -exec rm {} \;

# removes the generated REST service stubs
cleanRest:
	rm -rf $(REST_STUBS_LOCATION)

# compiles everything if needed and starts the REST service
run: packageIfNeeded
	 java -cp $(ARTEFACT_FILE) contract.server.Server

test: packageIfNeeded
	sbt test

# a way to quickly regenerate the diagrams, assuming you've already generated/compiled/packaged the REST stuff
# (e.g. ran packageIfNeeded)
generateDiagramsFast:
	 sbt --warn "runMain contract.diagram.generateMermaidDiagram" | tail -n +1 | bash

# this will create diagram.md and diagram-1.svg in the base directory
generateDiagrams: packageIfNeeded
	 java -cp $(ARTEFACT_FILE) contract.diagram.generateMermaidDiagram | tail -n +1 | bash

# jars up the REST stubs if they haven't been already
packageIfNeeded: $(PACKAGE_DEPENDENCY)

# creates a fat jar of this project
package: packageRestCode
	sbt assembly

# packages up / publishes locally (to .ivy) the REST stubs
testRestCode: generateRestCode
	cd $(REST_STUBS_LOCATION) && sbt --warn testRestCode

# packages up / publishes locally (to .ivy) the REST stubs
packageRestCode: generateRestCode
	cd $(REST_STUBS_LOCATION) && sbt --warn publishLocal

# generate the REST services from our ./service.yaml into a target directory
generateRestCode:
	# there are often changes to the scala-cask template, so it's useful to pull when needed:
	# docker pull openapitools/openapi-generator-cli:latest
	docker run --rm -v ${PWD}:/local openapitools/openapi-generator-cli:latest generate \
        -i /local/service.yaml \
        -g scala-cask \
        -c /local/openapi-config.yaml \
        -o /local/$(REST_STUBS_LOCATION)
