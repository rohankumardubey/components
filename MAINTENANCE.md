# TDP Maintenance Branch

## Dependency management

- `components.version` Maven property has been removed & replaced by Maven's built-in `project.version`
- Spring Boot dependencies have bean cleaned up from `components-bom`, keeping only the `spring-boot-starter-test` one
  since it seems to be used by some integration tests in components...
- Spring Boot has been bumped to 2.7 version
- `xlslx-streaming-reader` dependency has been changed to its up-to-date fork `excel-streaming-reader`.
    - Copied classes have been removed

## Tests

This works:

```
mvn clean install -DskipTests
mvn test
```

This doesn't work:

```
mvn clean install
```

## Ignored tests

> :warning: These tests were ignored because they were already broken. But we should definitely have a look at them
> and fix them if they're relevant or delete them otherwise.
