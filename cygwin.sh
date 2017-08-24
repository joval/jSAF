#!/bin/bash

if [ $# -eq 0 ]; then
  echo "Arguments must be supplied to the wraper"
  exit 1
fi

i=0
declare -A args=()
for arg; do
  if [ $i -eq 0 ]; then
    args[${#args[@]}]=$arg
  elif [[ ${arg} == *\;/* ]]; then
    IFS=';' read -a array <<< "$arg"
    for ((j=0; j < ${#array[@]}; j++)); do
      if [ -n "${array[$j]}" ]; then
        array[$j]=$(cygpath -w ${array[$j]})
      fi
    done
    args[${#args[@]}]=$( IFS=$';'; echo "${array[*]}" )
  elif [[ ${arg} == /* ]]; then
    args[${#args[@]}]=$(cygpath -w ${arg})
  else
    args[${#args[@]}]=$arg
  fi
  ((i++))
done
command=""
for ((i=0; i < ${#args[@]}; i++)); do
  command="$command \"${args[$i]}\""
done
eval ${command}
