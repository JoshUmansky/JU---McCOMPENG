#!/bin/bash

cd ~/project-group03-tuesday/src

# make if input argument is "c"
if [ "$1" == "c" ]; then
    make
fi

# Set default frequency
frequency="87.5M"

# Check if a second argument exists
if [ ! -z "$2" ]; then
    # If a second argument exists, use it to set the frequency
    frequency="$2"
fi

# Run rtl_sdr and project with the specified frequency
rtl_sdr -f "$frequency" -s 1.152M - | ./project 1 m | aplay -f S16_LE -c 1 -r 32000