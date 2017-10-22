# Trellis Application

[![Build Status](https://travis-ci.org/trellis-ldp/trellis-app.png?branch=master)](https://travis-ci.org/trellis-ldp/trellis-app)
[![Build status](https://ci.appveyor.com/api/projects/status/xu5qujp9ky2xq0uf?svg=true)](https://ci.appveyor.com/project/acoburn/trellis-app)
[![Coverage Status](https://coveralls.io/repos/github/trellis-ldp/trellis-app/badge.svg?branch=master)](https://coveralls.io/github/trellis-ldp/trellis-app?branch=master)

## Deployment

### Requirements

The trellis application requires both a [Zookeeper](http://zookeeper.apache.org) ensemble (3.5.x or later)
and a [Kafka](http://kafka.apache.org) cluster. The location of both is defined must be defined in a configuration
file, such as `config.yml` (see `config-sample.yml` for an example). Java 8 or 9 is required.

### Starting Trellis

1. Run `./gradlew clean build` to build the application or download one of the releases.
2. Start application with `java -jar build/libs/trellis.jar server config.yml`
3. To check that trellis is running, check the URL: `http://localhost:8080`

### Health Checks

Application health checks are available at `http://localhost:8081/healthcheck`
