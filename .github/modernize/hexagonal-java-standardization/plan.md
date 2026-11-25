# Modernization Plan: Hexagonal Architecture and Java Standardization

**Project**: PW2-ProjetoFinal

---

## Technical Framework

- **Language**: Java 21 configured; target release subject to compatibility
  verification
- **Framework**: Quarkus 3.29.4 in three independent services
- **Build Tool**: Maven wrappers and independent service POMs
- **Database**: MySQL 8.0
- **Key Dependencies**: Quarkus REST, Hibernate Reactive/Panache, SmallRye JWT,
  REST Client, OpenTelemetry
- **Services**: `users`, `gastos`, and `gateway`
- **Observed package roots**: `dev.ifrs`, `run.gastos`, and `br.com.gateway`;
  build metadata also contains `org.acme:code-with-quarkus` and
  `br.com:api-gateway`. Confirm the actual source and build evidence before
  choosing normalized package and artifact names.

---

## Overview

This work modernizes the structure and Java configuration of the existing
microservices without changing their service boundaries or externally visible
behavior. It will:

- Establish pragmatic hexagonal boundaries within each service.
- Align Java package names and source directories with the evidence-backed
  service naming convention.
- Standardize the Java toolchain, runtime/container base images, and related
  documentation across all services.

Java 25 is the preferred target only if compatibility is verified against the
repository's Quarkus version, Maven/build tooling, dependencies, and runtime
images. Otherwise, use the latest stable Java release that is demonstrably
compatible; do not assume compatibility or upgrade Quarkus as an unrelated
change. Preserve endpoint contracts, persistence behavior, authentication,
inter-service communication, ports, and observability.

---

## Migration Impact Summary

| App     | Current           | Target          | Auth | Impact        |
|---------|-------------------|-----------------|------|---------------|
| users   | Java 21 / Quarkus | No cloud change | JWT  | Internal only |
| gastos  | Java 21 / Quarkus | No cloud change | JWT  | Internal only |
| gateway | Java 21 / Quarkus | No cloud change | JWT  | Internal only |

---

## Open Questions & Questionnaire

No unresolved user decisions are required. The Java target is a compatibility
gate: execution must record evidence for Java 25; if it is not verified, use
the latest stable compatible release and document the reason.
