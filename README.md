# Graal Cloud Native (GCN)

Graal Cloud Native (GCN) is a full stack framework for building portable cloud-native applications using the Java ecosystem on top of managed services in the public cloud. GCN is built on top of the GraalVM native image, the Micronaut® framework, public cloud SDKs, popular drivers, and a set of open source libraries tested, certified, and supported by Oracle. Oracle has optimized this stack for native image as well as for cloud portability. With GCN, cloud users can focus less on language portability and more on portability across cloud vendors.

## Installation

The GCN CLI will be available soon. In the meantime, use the [Launcher on the GCN website][launcher] to create GCN applications.

## Documentation

Please refer to the [GCN website for documentation][docs].

## Examples

Check out our collection of [guides and examples][guides].

## Help

* Open a [GitHub issue][issues] for bug reports, questions, or requests for enhancements.
* Report a security vulnerability according to the [Reporting Vulnerabilities guide][reporting-vulnerabilities].

## Repository Structure

This source repository is the main repository for GCN and includes the following components:

Directory | Description
------------ | -------------
[`buildSrc/`](buildSrc/) | Gradle build and convention plugins.
[`config/`](config/) | Configuration files for Gradle code quality plugins.
[`gcn-cli/`](gcn-cli/) | The GCN CLI, a Micronaut [Picocli](https://picocli.info/) application that generates GCN applications.
[`gcn-core/`](gcn-core/) | The main GCN library.

## Contributing

This project welcomes contributions from the community. Before submitting a pull request, please [review our contribution guide](./CONTRIBUTING.md)

## Security

Please consult the [security guide](./SECURITY.md) for our responsible security vulnerability disclosure process

## License

Copyright (c) 2023 Oracle and/or its affiliates.

GCN is open source and distributed under the [Apache License version 2.0](LICENSE.txt).

[docs]: https://www.graal.cloud/gcn/
[guides]: https://www.graal.cloud/gcn/guides/
[issues]: https://github.com/oracle/gcn/issues
[launcher]: https://www.graal.cloud/gcn/launcher/
[reporting-vulnerabilities]: https://www.oracle.com/corporate/security-practices/assurance/vulnerability/reporting.html
