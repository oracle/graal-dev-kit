# Graal Development Kit for Micronaut (GDK)

[Graal Development Kit for Micronaut (GDK)](https://graal.cloud/gdk/) is a curated set of open source Micronaut® framework modules designed from the ground up to be compiled ahead-of-time with GraalVM Native Image resulting in native executables ideal for microservices.
With GDK, you can build portable cloud-native Java microservices that start instantly and use fewer resources to reduce compute costs.

## Installation

You can use GDK by generating an application using either the [Launcher on the GDK website][launcher] or install the CLI using the package managers below:

### MacOS

##### Homebrew

Setup the tap for the cask:

```sh
brew tap oracle/graal-dev-kit https://github.com/oracle/graal-dev-kit
```

Then install the GDK CLI tool:

```sh
brew install gdk-<major-version>.<minor-version>
```

Examples :

```sh
brew install gdk-4.9 
```

```sh
brew install gdk-4.7 
```

##### SDKMAN

Get [SDKMAN](https://sdkman.io)  (Always make sure to review content of the bash file)
```sh
curl -s "https://get.sdkman.io" | bash
```
Install GDK CLI

```sh
sdk install gcn
```

### Linux

##### SDKMAN

Get [SDKMAN](https://sdkman.io)  (Always make sure to review content of the bash file)
```sh
curl -s "https://get.sdkman.io" | bash
```
Install GDK CLI

```sh
sdk install gcn
```

### Other Platforms

Get the binary from https://github.com/oracle/graal-dev-kit/releases

## Documentation

Please refer to the [GDK website for documentation][docs].

## Examples

Check out our collection of [guides and examples][guides].

## Help

* Open a [GitHub issue][issues] for bug reports, questions, or requests for enhancements.
* Report a security vulnerability according to the [Reporting Vulnerabilities guide][reporting-vulnerabilities].

## Repository Structure

This source repository is the main repository for GDK and includes the following components:

Directory | Description
------------ | -------------
[`buildSrc`](buildSrc/) | Gradle build and convention plugins.
[`config`](config/) | Configuration files for Gradle code quality plugins.
[`gdk-cli`](gdk-cli/) | The GDK CLI, a Micronaut [Picocli](https://picocli.info/) application that generates GDK applications.
[`gdk-cli-core`](gdk-cli-core/) | The core CLI classes, separated from `gdk-cli` to enable creating alternate CLIs.
[`gdk-core`](gdk-core/) | The main GDK library.

## Contributing

This project welcomes contributions from the community. Before submitting a pull request, please [review our contribution guide](./CONTRIBUTING.md)

## Security

Please consult the [security guide](./SECURITY.md) for our responsible security vulnerability disclosure process

## License

Copyright (c) 2023 Oracle and/or its affiliates.

GDK is open source and distributed under the [Apache License version 2.0](LICENSE.txt).

[docs]: https://graal.cloud/gdk/
[guides]: https://graal.cloud/gdk/guides/
[issues]: https://github.com/oracle/graal-dev-kit/issues
[launcher]: https://graal.cloud/gdk/launcher/
[reporting-vulnerabilities]: https://www.oracle.com/corporate/security-practices/assurance/vulnerability/reporting.html
