# COEN448 Assignment 1

This repository contains the COEN448 assignment project.

## What I changed
- Cleaned up repository by adding a `.gitignore` and removing tracked IDE/build artifacts.
- Added an OpenRewrite configuration (`rewrite.yml`) to enable an automated recipe to help migrate code to Java 21 when applicable.
- updated java upgrade specs

## Prerequisites
- Java (JDK) 21 for final runtime target (development used JDK 11 for OpenRewrite application).
- Maven 3.6+ to build the project.

## Build & Test
From the repository root, run:

```bash
mvn clean test
```

If you use OpenRewrite recipes locally to migrate to Java 21, you can place/keep `rewrite.yml` at the project root and run OpenRewrite with the Maven plugin.

## Notes
- The repository was cleaned of IDE and generated files (e.g., `.classpath`, `target/`, `.clover/`, `*.zip`). See `.gitignore` for details.
- I added `rewrite.yml` containing the `org.openrewrite.java.migrate.UpgradeToJava21` recipe to help with any Java 21 code updates. Applying the recipe requires Maven and a compatible JDK for the rewrite run.

## Contact
If you want additional changes (apply OpenRewrite migrations, update build tool settings, or migrate CI), tell me what you prefer and I can proceed.
