#!/bin/sh

set -e

javac **/*.java
javac *.java
./halite --replay-directory replays/ -vvv --width 32 --height 32 "java MyBot" "java MyBot"
