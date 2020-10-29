#!/bin/bash

echo publishing thread-core
./gradlew :thread-core:bintrayUpload

echo publishing thread-epic
./gradlew :thread-epic:bintrayUpload

echo publishing thread-flipper
./gradlew :thread-flipper:bintrayUpload