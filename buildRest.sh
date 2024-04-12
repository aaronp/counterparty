#!/usr/bin/env bash

docker run --rm -v ${PWD}:/local openapitools/openapi-generator-cli:latest generate \
    -i /local/service.yaml \
    -g scala-cask \
    -c /local/openapi-config.yaml \
    -o /local/server

pushd server
echo "building REST service"
sbt publishLocal
popd
