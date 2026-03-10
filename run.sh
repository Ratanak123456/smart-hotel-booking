#!/bin/bash
javac -cp "lib/*:src" -d out/production/hotel-reservation-system src/App.java src/model/entities/*.java src/model/dao/*.java src/model/service/*.java src/controller/*.java src/view/*.java src/db/*.java src/dto/*.java src/exception/*.java src/mapper/*.java
java -cp "lib/*:out/production/hotel-reservation-system" App
