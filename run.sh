#!/bin/bash
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

javac -cp "lib/*:src" -d out/production/hotel-reservation-system src/App.java src/model/entities/*.java src/model/dao/*.java src/model/service/*.java src/controller/*.java src/view/*.java src/db/*.java src/dto/*.java src/exception/*.java src/mapper/*.java
mkdir -p out/production/hotel-reservation-system/resources
cp src/resources/invoice.jrxml out/production/hotel-reservation-system/resources/
java -cp "lib/*:out/production/hotel-reservation-system:out/production/hotel-reservation-system/resources" App
