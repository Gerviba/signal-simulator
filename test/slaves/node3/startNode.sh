#!/bin/bash
parent_path=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )
cd "$parent_path"
cp ../../../target/simulator-*.jar ./simulator.jar
screen -dmS NODE3 java -Dspring.profiles.active=slave -jar simulator.jar -Dspring.profiles.active=slave
screen -r NODE3
