# jinjin-quick-delivery

This project is a Spring Boot application for quick delivery service.

## Documentation

- [Usage Guide](doc/UsageGuide.md) - How to run the application and tests.

## Quick Start

### Run the Application
```bash
./run.sh --mode mvn --profile dev
```

### Run All Tests
```bash
mvn clean test
```

### Run Endpoint QA Suite
```bash
mvn -pl jinjin-server test
```

The endpoint QA tests use the `test` Spring profile. They boot the full application context with `MockMvc`, use an isolated in-memory H2 database initialized from `jinjin-server/src/test/resources/schema-test.sql`, and use Redis database `15` for test-only Redis state.
