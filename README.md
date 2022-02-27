<h1 align="center">MCSV.KR Platform - Client</h1>
<p align="center">The All-in-One Platform for Minecraft Server Sysadmins</p>
<p align="right">built by Stella IT Inc. with ❤</p>
<hr />

## What does it do?
This plugin is for integrating your server to MCSV.KR Platform dashboard which allows users to control their minecraft server from dashboard or equivalent api calls.  

## Nightly Builds
* **1.8~1.15.2**: [![Nightly Builds for Legacy (1.15.2 codebase)](https://github.com/mcsv-kr/mcsv-client/actions/workflows/build-1.8-legacy.yml/badge.svg)](https://github.com/mcsv-kr/mcsv-client/actions/workflows/build-1.8-legacy.yml)
* **1.16.5**: [![Nightly Builds (Java 8)](https://github.com/mcsv-kr/mcsv-client/actions/workflows/build-1.8.yml/badge.svg)](https://github.com/mcsv-kr/mcsv-client/actions/workflows/build-1.8.yml)
* **1.17+**: [![Nightly Builds (Java 17)](https://github.com/mcsv-kr/mcsv-client/actions/workflows/build.yml/badge.svg)](https://github.com/mcsv-kr/mcsv-client/actions/workflows/build.yml)

## Target System
Due to legacy compatibility, MCSV.KR Client targets Java 8 by default.  

But in the near future, MCSV.KR Client will target Java 17 for improved support for [PaperMC](https://papermc.io) and [Minecraft 1.17+](https://www.minecraft.net/en-us/article/minecraft-snapshot-21w19a).  
This will cause current 1.16.5 support won't be on mainline since it will be using `legacy/1.16.5` instead of `main` due to Spigot API's missing support for `Java 1.17` for `Minecraft 1.16.5`.  

Currently, MCSV.KR Client's `main` branch targets Java 8 with Minecraft version 1.16.5. with compatibility for 1.17+.  

## License
Distributed under [MIT License](LICENSE)
