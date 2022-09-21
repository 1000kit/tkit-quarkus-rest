# tkit-quarkus-rest

1000kit Quarkus REST extension

[![License](https://img.shields.io/badge/license-Apache--2.0-green?style=for-the-badge&logo=apache)](https://www.apache.org/licenses/LICENSE-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/org.tkit.quarkus/tkit-quarkus-rest?logo=java&style=for-the-badge)](https://maven-badges.herokuapp.com/maven-central/org.tkit.quarkus/tkit-quarkus-rest)
[![GitHub Actions Status](<https://img.shields.io/github/workflow/status/1000kit/tkit-quarkus-rest/build?logo=GitHub&style=for-the-badge>)](https://github.com/1000kit/tkit-quarkus-rest/actions/workflows/build.yml)

## Documentation

Example project with this extension is in the [1000kit JPA guides](https://1000kit.gitlab.io/guides/docs/quarkus/quarkus-jpa-project/)

This 1000kit extension contains `DTO` and `Exception` for the `REST` interface.

### OpenID connect interceptor

```properties
rest-client/mp-rest/url=https://service
rest-client/mp-rest/providers=org.tkit.quarkus.rs.interceptors.OpenIDConnectInterceptor
```
### Config

| Property | Env | Default | Values | Description |
| ------------- | ------------- | ------------- | ------------- | ------------- |
| tkit.rs.mapper.log | TKIT_RS_MAPPER_LOG | true | true,false | Disable or enable log in the exception mapper | 

### DTO

All `DTO` classes are in the package `org.tkit.quarkus.rs.models`. These classes have weak reference to the
`Entity` classes and `PageResult` class in the [tkit-quarkus-jpar]() extension.
The class `RestExceptionDTO` is for the `RestException`.

### Exception

This extension define the `RestException` which should be use in the `RestController`. This exception support 
translation for the `errorCode` with `parameters`. The local is taken from the HTTP request. For default value use
Quarkus configuration value `quarkus.default-locale`. 
Exception is in the package `org.tkit.quarkus.rs.exceptions`

### Mappers

In the package `org.tkit.quarkus.rs.mappers` is the `ExceptionMapper` for all `Exception` in the project with priority `10000`.
You can `extend` and overwrite this `ExceptionMapper`. Use the `@Priority` to overwrite existing registration.

## Create a release

```bash
mvn semver-release:release-create
```

### Create a patch branch
```bash
mvn semver-release:patch-create -DpatchVersion=x.x.0
```
