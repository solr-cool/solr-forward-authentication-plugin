# Solr Forward Authentication Plugin

[![continuous integration](https://github.com/solr-cool/solr-forward-authentication-plugin/actions/workflows/ci.yaml/badge.svg)](https://github.com/solr-cool/solr-forward-authentication-plugin/actions/workflows/ci.yaml)
[![Maven Central](https://img.shields.io/maven-central/v/cool.solr/solr-forward-authentication-plugin)](https://search.maven.org/artifact/cool.solr/solr-forward-authentication-plugin/)


A simple forward authentication plugin for Solr. _Forward authentication_ moves
the authentication process out of Solr into a reverse proxy like
[Traefik](https://doc.traefik.io/traefik/middlewares/http/forwardauth/) or
[Nginx](https://docs.nginx.com/nginx/admin-guide/security-controls/configuring-subrequest-authentication/) running in front of Solr.

After authentication, the authenticated user is sent to Solr via a HTTP header.
This plugins lets Solr accept this header and set the authenticated user
accordingly.


![Forward Authentication](https://doc.traefik.io/traefik/assets/img/middleware/authforward.png)

## How to use the plugin

> Before using the plugin, please be familiar with
> [Solr authentication and authorization](https://solr.apache.org/guide/8_11/authentication-and-authorization-plugins.html).

### Install the plugin

You can either drop the
[release jar](https://github.com/solr-cool/solr-forward-authentication-plugin/releases)
into the library directory of your Solr installation or install this plugin
using the [Solr plugin system](https://solr.apache.org/guide/8_11/solr-plugins.html):

```shell
bin/solr package add-repo solr-forward-auth \
    "https://raw.githubusercontent.com/solr-cool/solr-forward-authentication-plugin/main/repo/"
bin/solr package install solr-forward-authentication
bin/solr package deploy solr-forward-authentication -y -cluster
```

#### Updating to a newer version
To check installed version and available versions of the package,

```shell
bin/solr package list-installed
bin/solr package list-available
```

To update to a newer version,

```shell
bin/solr package install solr-forward-authentication:<new-version>
bin/solr package deploy solr-forward-authentication:<new-version> -y -cluster -update
```

#### Undeploying

```shell
bin/solr package undeploy solr-forward-authentication -cluster
```

### Configure authentication

```json
{
    "authentication": {
        "class": "cool.solr.security.ForwardAuthPlugin",
        "httpUserHeader": "X-Forwarded-User"
    }
}
```

### Configure authorization

```json
{
    "authentication": {
        "class": "cool.solr.security.ForwardAuthPlugin",
        "httpUserHeader": "X-Forwarded-User"
    },
    "authorization": {
        "class": "cool.solr.security.DefaultRuleBasedAuthorizationPlugin",
        "defaultRole": "admin",
        "permissions": [
            {
                "name": "all",
                "role": "admin"
            }
        ]
    }
}
```

### Example

The [`examples`](examples/) folder contains a simple Docker Compose ensemble.
From inside the directory, launch the Solr/Zookeeper ensemble:

```bash
$ docker-compose up

# Test connectivity (should return 200 OK)
$ curl -I http://localhost:8983/solr/ping

# Install forward authentication plugin
docker exec -it solr solr package add-repo solr-forward-authentication "https://raw.githubusercontent.com/solr-cool/solr-forward-authentication-plugin/main/repo/"
docker exec -it solr solr package install solr-forward-authentication -cluster
docker exec -it solr solr package deploy solr-forward-authentication -y -cluster

# Activate Solr security
$ docker exec -it solr solr zk cp file:/opt/solr/server/solr/security.json zk:/security.json -z zookeeper:2181

# Test security (should return 401)
$ curl -I http://localhost:8983/solr/ping

# Fake forward authentication (should return 200)
$ curl -I -H "X-Forwarded-User: alice" http://localhost:8983/solr/ping
```

## Building the project

This should install the current version into your local repository

    $ ./mvn clean verify

### Updating the Solr repo

```shell
$ openssl genrsa -out solr-repo.pem 512
$ openssl rsa -in solr-repo.pem -pubout -outform DER -out repo/publickey.der
$ curl -sfLo target/release.jar RELEASE_URL
$ openssl dgst -sha1 -sign solr-repo.pem target/release.jar | openssl enc -base64 | tr -d \\n | sed

```

solr zk cp file:/opt/solr/server/solr/clusterprops.json zk:/clusterprops.json -z zookeeper:2181
solr package add-repo solr-forward-authentication http://repo:8080
solr package install solr-forward-authentication -cluster
solr package deploy solr-forward-authentication -y -cluster
solr zk cp file:/opt/solr/server/solr/security.json zk:/security.json -z zookeeper:2181


## License

This project is licensed under the [Apache License, Version 2](http://www.apache.org/licenses/LICENSE-2.0.html).
