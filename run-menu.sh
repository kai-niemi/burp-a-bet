#!/bin/bash

######################################
# Do not edit below
######################################

if [ ! -f "$jarfile" ]; then
    ./mvnw clean install
fi

PS3='Please select profiles: '
options=( "local" "dev" "<Start>" "<Quit>" )

select option in "${options[@]}"; do
  case $option in
    "<Start>")
      break
      ;;
    "<Quit>")
      exit 0
      ;;
    *)
      if [ -z "${profiles}" ]; then
      profiles=$option
      else
      profiles=$profiles,$option
      fi
      echo -e "Selected profiles: $profiles (press enter)"
      ;;
  esac
done

echo -e java -jar ${jarfile} --spring.profiles.active=${profiles} ${params} $*

java -jar ${jarfile} --spring.profiles.active=${profiles} ${params} $*
