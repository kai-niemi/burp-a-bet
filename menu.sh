#!/bin/bash

if [ ! -f "$jarfile" ]; then
    ./mvnw clean install
fi

PS3='Please select profiles: '
options=( "<Start>" "local" "dev" )

select option in "${options[@]}"; do
  case $option in
    "<Start>")
      break
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

if [ -z "${profiles}" ]; then
  echo -e java -jar ${jarfile} ${params} $*
  java -jar ${jarfile} ${params} $*
else
  echo -e java -jar ${jarfile} --spring.profiles.active=${profiles} ${params} $*
  java -jar ${jarfile} --spring.profiles.active=${profiles} ${params} $*
fi

