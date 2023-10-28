# Experimental Epsilon CLI tool

This is an experiment on a command-line tool to run [Epsilon](http://eclipse.org/epsilon) programs.
At the moment, it only supports running EOL scripts on EMF models, using `.ecore` or `.emf` ([Emfatic](https://eclipse.dev/emfatic/)) metamodels.

## Building the program

To run a Java version of this program, run this command:

```sh
./gradlew build
```

The built distributions will be in the `build/distributions` folder.

## Building the native image

To build a GraalVM native image of this program, install GraalVM 17 or later (e.g. via [SDKMAN](https://sdkman.io/)), and run this command:

```sh
./gradlew nativeCompile
```

The built native image will be the executable in `build/native/nativeCompile/epsilon`.

## Micronaut 4.1.5 Documentation

- [User Guide](https://docs.micronaut.io/4.1.5/guide/index.html)
- [API Reference](https://docs.micronaut.io/4.1.5/api/index.html)
- [Configuration Reference](https://docs.micronaut.io/4.1.5/guide/configurationreference.html)
- [Micronaut Guides](https://guides.micronaut.io/index.html)

### Gradle plugins

- [Micronaut Gradle Plugin documentation](https://micronaut-projects.github.io/micronaut-gradle-plugin/latest/)
- [GraalVM Gradle Plugin documentation](https://graalvm.github.io/native-build-tools/latest/gradle-plugin.html)
- [Shadow Gradle Plugin](https://plugins.gradle.org/plugin/com.github.johnrengelman.shadow)
