# kotlin-durable-workflows

Durable **Workflows-as-Code** engine for Kotlin, powered by Coroutines and a Redis-like key-value store.

## What this is

This library lets you run long-lived workflows (timers, retries, signals, state) safely across restarts and multiple workers, with minimal runtime overhead.

## Features

- Durable workflow execution with horizontal scaling support
- Durable timers for notifications and delayed jobs
- Low overhead event loop (designed for lots of concurrent workflows)
- Load-tested to run **100K+** workflows simultaneously (see [`load-test`](./load-test))

## Install

### GitHub Packages (recommended for private/internal use)

`build.gradle.kts`

```kotlin
plugins {
    id("net.saliman.properties") version "1.5.2"
}

repositories {
    mavenCentral()

    maven {
        url = uri("https://maven.pkg.github.com/<YOUR_GITHUB_ORG>/kotlin-durable-workflows")

        credentials {
            username = project.findProperty("gpr.user") as String?
            password = project.findProperty("gpr.key") as String?
        }
    }
}

dependencies {
    implementation("thedevibrahim:workflows:<version>")
}
```

`gradle-local.properties`

```properties
gpr.user=YOUR_GITHUB_USERNAME
gpr.key=YOUR_GITHUB_TOKEN
```

> Tip: add `gradle-local.properties` to `.gitignore`.

## Usage

See [`example-app`](./example-app) for a working example.

## Deploy notes

- Use `HOSTNAME` (or another stable identifier) as `workerId`.
- For Kubernetes, StatefulSets are a good default when you want stable worker identities.

## Supported platforms

- Java **21+** (LTS)

## Official supported databases and clients

| DB                       | Client                                       |
| ------------------------ | -------------------------------------------- |
| Redis Standalone v7.4.2+ | [LettuceRedisClient][LettuceRedisClient] |
| Redis Standalone v7.4.2+ | [ReThisRedisClient][ReThisRedisClient]   |

> In most cases, prefer [ReThisRedisClient][ReThisRedisClient].

### Custom clients

Implement [KeyValueClient][KeyValueClient] to support other Redis-like databases.

## Maintainers

- [@thedevibrahim](https://github.com/thedevibrahim)

## License

MIT License

[KeyValueClient]: ./src/main/kotlin/thedevibrahim/workflows/core/interfaces/KeyValueClient.kt
[LettuceRedisClient]: ./src/main/kotlin/thedevibrahim/workflows/clients/LettuceRedisClient.kt
[ReThisRedisClient]: ./src/main/kotlin/thedevibrahim/workflows/clients/ReThisRedisClient.kt
