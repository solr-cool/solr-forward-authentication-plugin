#!/bin/bash
#
# copy repo key
cp repo/publickey.der target/

# get signature and date
TODAY=$(date +%Y-%m-%d)
SIG=$(openssl dgst -sha1 -sign solr-repo.pem target/solr-forward-authentication-plugin-0-SNAPSHOT.jar | openssl enc -base64 | tr -d \\n | sed)

# build repository.json
cat << EOF > target/repository.json
[
  {
    "name": "solr-forward-authentication",
    "description": "A forward authentiction plugin",
    "versions": [
      {
        "version": "0.0.1",
        "date": "${TODAY}",
        "artifacts": [
          {
            "url": "http://repo:8080/solr-forward-authentication-plugin-0-SNAPSHOT.jar",
            "sig": "${SIG}"
          }
        ]
      }
    ]
  }
]
EOF
