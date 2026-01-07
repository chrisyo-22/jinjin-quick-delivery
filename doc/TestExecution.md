# Test Execution Guide

This guide explains how to run tests in the **jinjin-quick-delivery** Maven multi-module project.

---

## Prerequisites

Ensure `JAVA_HOME` is set correctly (JDK directory, not the executable):

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
```

To make it permanent, add to `~/.bashrc`:

```bash
echo 'export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64' >> ~/.bashrc
source ~/.bashrc
```

---

## Command Breakdown

### Example: `mvn -pl jinjin-server -Dtest=TestRedis test`

| Part | Description |
|------|-------------|
| `mvn` | Maven command-line tool |
| `-pl jinjin-server` | **Project List** — run only in the `jinjin-server` module |
| `-Dtest=TestRedis` | **System Property** — specify which test class to run |
| `test` | Maven lifecycle phase that compiles and runs tests |

---

## Running Tests

### Run All Tests (Entire Project)

From the project root:

```bash
mvn test
```

This runs tests in all modules (`jinjin-common`, `jinjin-pojo`, `jinjin-server`).

### Run All Tests in a Specific Module

```bash
mvn -pl jinjin-server test
```

### Run a Single Test Class

```bash
mvn -pl jinjin-server -Dtest=TestRedis test
```

### Run a Single Test Method

```bash
mvn -pl jinjin-server -Dtest=TestRedis#testRedisTemplateLoaded test
```

### Run Multiple Test Classes

```bash
mvn -pl jinjin-server -Dtest=TestRedis,TestOther test
```

### Run Tests Matching a Pattern

```bash
# All tests starting with "Test"
mvn -pl jinjin-server -Dtest=Test* test

# All tests in a package
mvn -pl jinjin-server -Dtest=com.jinjin.** test
```

---

## Running Test Suites

If you have a JUnit 5 `@Suite` class (e.g., `AllTestsSuite`):

```bash
mvn -pl jinjin-server -Dtest=AllTestsSuite test
```

Example suite class:

```java
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({TestRedis.class, TestOther.class})
public class AllTestsSuite {
}
```

---

## Useful Maven Test Options

| Option | Description |
|--------|-------------|
| `-DskipTests` | Compile tests but skip execution |
| `-Dmaven.test.skip=true` | Skip test compilation and execution |
| `-Dtest=ClassName` | Run specific test class |
| `-Dtest=ClassName#method` | Run specific test method |
| `-DfailIfNoTests=false` | Don't fail if no tests found |
| `-Dsurefire.useFile=false` | Print test output to console |
| `-X` | Debug mode (verbose output) |

### Show Test Output in Console

```bash
mvn -pl jinjin-server -Dtest=TestRedis test -Dsurefire.useFile=false
```

### Continue After Test Failures

```bash
mvn test -Dmaven.test.failure.ignore=true
```

---

## Running Tests from VS Code

### Option 1: Test Runner Extension (Recommended)

1. Install **Extension Pack for Java** (`vscjava.vscode-java-pack`)
2. Open a test file — you'll see **Run Test | Debug Test** links above each `@Test` method
3. Use the **Testing** panel (beaker icon in sidebar) to browse and run all tests

### Option 2: Terminal

Open VS Code terminal (`Ctrl+``) and run Maven commands as shown above.

---

## Common Issues

### `JAVA_HOME is not defined correctly`

**Cause:** `JAVA_HOME` points to the Java executable instead of the JDK directory.

**Fix:**
```bash
# Wrong
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64/bin/java

# Correct
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
```

### Test Output Not Visible

**Cause:** Maven Surefire writes output to files by default.

**Fix:** Add `-Dsurefire.useFile=false` or use assertions instead of `System.out.println()`.

### Spring Context Fails to Load

**Cause:** Database/Redis not running, or `application-dev.yml` credentials are wrong.

**Fix:** Ensure MySQL and Redis are running, or use slice tests (`@DataRedisTest`, `@DataJpaTest`) that don't load the full context.

### Test Changes Not Reflected

**Cause:** Maven uses cached/stale compiled `.class` files from the `target/` directory.

**Fix:** Add `clean` before `test` to delete old compiled classes and force recompilation:

```bash
# Instead of:
mvn -pl jinjin-server -Dtest=TestRedis test

# Use:
mvn -pl jinjin-server -Dtest=TestRedis clean test
```

**Alternative:** If you only want to recompile without cleaning everything:

```bash
mvn -pl jinjin-server test-compile test -Dtest=TestRedis
```

**Tip:** The `clean` phase deletes the `target/` folder, ensuring all code is freshly compiled.

---

## Quick Reference

```bash
# All tests (with clean to pick up code changes)
mvn clean test

# Single module
mvn -pl jinjin-server clean test

# Single class
mvn -pl jinjin-server -Dtest=TestRedis clean test

# Single method
mvn -pl jinjin-server -Dtest=TestRedis#testRedisTemplateLoaded clean test

# With console output
mvn -pl jinjin-server -Dtest=TestRedis clean test -Dsurefire.useFile=false
```
