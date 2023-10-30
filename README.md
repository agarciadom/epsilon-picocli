# Command-line Epsilon interpreter

Provides a [Picocli](https://picocli.info/)-based command line tool for executing Epsilon scripts

Uses [Micronaut Picocli](https://micronaut-projects.github.io/micronaut-picocli/latest/guide/) for automatic configuration and generation of [GraalVM native images](https://www.graalvm.org/latest/reference-manual/native-image/).

## Building and trying out the Java-based version

To build as a plain Java program, run this command:

```shell
./gradlew build
```

You can then use the distributions in `build/distributions`, or try the script directly from Gradle with:

```shell
./gradlew run --args="example/program.eol -f example/model.flexmi -m example/metamodel.emf -r"
```

## Building and using the native image

To build the native image, install a GraalVM JDK (e.g. via [SDKMAN](https://sdkman.io/)) and run this command:

```shell
./gradlew nativeCompile
```

You can then try out the native image with a command like this one:

```shell
build/native/nativeCompile/epsilon example/program.eol \
  -f example/model.flexmi -m example/metamodel.emf -r
```

## Micronaut 4.1.5 Documentation

- [User Guide](https://docs.micronaut.io/4.1.5/guide/index.html)
- [API Reference](https://docs.micronaut.io/4.1.5/api/index.html)
- [Configuration Reference](https://docs.micronaut.io/4.1.5/guide/configurationreference.html)
- [Micronaut Guides](https://guides.micronaut.io/index.html)

### Gradle plugins

- [Micronaut Gradle Plugin documentation](https://micronaut-projects.github.io/micronaut-gradle-plugin/latest/)
- [GraalVM Gradle Plugin documentation](https://graalvm.github.io/native-build-tools/latest/gradle-plugin.html)
- [Shadow Gradle Plugin](https://plugins.gradle.org/plugin/com.github.johnrengelman.shadow)
