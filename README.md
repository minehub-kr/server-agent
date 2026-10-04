<h1 align="center">Minehub ServerAgent</h1>
<p align="center">The All-in-One Platform for Minecraft Server Sysadmins</p>
<p align="right">built by <a href="https://stella-it.com">Stella IT Inc.</a> with ❤</p>
<hr />

## What does it do?
This plugin is for integrating your server to [Minehub dashboard](https://dash.minehub.kr) which allows users to control their minecraft server from dashboard or equivalent api calls.  

## Nightly Builds
[![Nightly Builds (Java 17)](https://github.com/minehub-kr/server-agent/actions/workflows/build.yml/badge.svg)](https://github.com/minehub-kr/server-agent/actions/workflows/build.yml)

## Target System
Minehub Agent targets Java 17 by default.  

## Compatibility checks
Run the unit tests and build the runtime test plugin in Docker:

```sh
docker run --rm \
  -e GRADLE_USER_HOME=/workspace/build/compatibility/gradle-cache \
  -v "$PWD":/workspace -w /workspace eclipse-temurin:17-jdk \
  bash ./gradlew --no-daemon test shadowJar compatibilityProbeJar
```

The test servers use separate worlds and do not publish host ports. The Compose
configuration accepts the Minecraft EULA for these local test servers. Run one
service at a time: `paper26` (Java 25), `paper121` (Java 21), `paper119`, `paper118`,
`paper117` (Java 17), or `paper116` (Java 11).

```sh
docker compose -f docker-compose.compatibility.yml up -d paper26
docker compose -f docker-compose.compatibility.yml logs -f paper26
docker compose -f docker-compose.compatibility.yml down
```

The probe checks command output, invalid-command handling, world heights, metadata,
and log forwarding. It writes `results.json` under
`build/compatibility/fixed-26.3/plugins/MinehubCompatibilityProbe/` for `paper26`.
The other services use `fixed-<Minecraft version>` directories.
These checks do not authenticate to Minehub or register a server.

Command feedback uses Paper's sender API when available. Older servers fall back
to console logs from the command's thread while the command executes, preferring
direct sender feedback when present. Output logged later by asynchronous tasks
continues through the existing server log stream.

The existing 1.8.8 and 1.12.2 legacy source patches can also be compiled against
their original Spigot APIs without modifying the checkout:

```sh
bash scripts/test-legacy-builds.sh
```

This verifies legacy compilation; it does not certify runtime support for those
versions. Their existing CI entries are marked as unsupported.

## License
Distributed under [MIT License](LICENSE)
