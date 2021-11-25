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

### Installation

There are two types of installation possible

1. Either place [release jar](https://github.com/solr-cool/solr-forward-authentication-plugin/releases)
in your Solr library directory.
1. Plugin installation

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

## License

This project is licensed under the [Apache License, Version 2](http://www.apache.org/licenses/LICENSE-2.0.html).
