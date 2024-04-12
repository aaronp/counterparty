# Contracts example

Example for business logic which can be converted into a mermaid diagram

## Running

This project can be built using [scala-cli](https://scala-cli.virtuslab.org/):
```sh
scala-cli ./src/main/scala
```

or with [sbt](https://www.scala-sbt.org/):
```sh
sbt run
```

## Building

This project can be built using [scala-cli](https://scala-cli.virtuslab.org/):
```sh
scala-cli --power package ./src/main/scala --assembly -o app.jar
```

or with a zero-install docker build:
```sh
docker run --rm -v "${PWD}/src/main/scala:/home" virtuslab/scala-cli package /home --power --assembly -o /home/app.jar
```

or with [sbt](https://www.scala-sbt.org/):
```sh
sbt package
```