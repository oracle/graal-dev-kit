# Graal Cloud Native (GCN)

Graal Cloud Native (GCN) is a curated set of Micronaut™ framework modules designed from the ground up to be compiled ahead-of-time with GraalVM Native Image resulting in native executables ideal for microservices.
With GCN, you can build portable cloud-native Java microservices that start instantly and use fewer resources to reduce compute costs.

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
[`gcn/buildSrc/`](gcn/buildSrc/) | Gradle build and convention plugins.
[`gcn/config/`](gcn/config/) | Configuration files for Gradle code quality plugins.
[`gcn/gcn-cli/`](gcn/gcn-cli/) | The GCN CLI, a Micronaut [Picocli](https://picocli.info/) application that generates GCN applications.
[`gcn/gcn-core/`](gcn/gcn-core/) | The main GCN library.

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
