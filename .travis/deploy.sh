#!/usr/bin/env bash
if [ "$TRAVIS_BRANCH" = 'master' ] && [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
    openssl aes-256-cbc -K $encrypted_be481c4eeabe_key -iv $encrypted_be481c4eeabe_iv -in codesigning.asc.enc -out codesigning.asc -d
    gpg --fast-import cd/signingkey.asc

    mvn deploy -P sign,build-extras --settings .travis/mvnsettings.xml
fi
