#!/bin/bash

#cd ~/Desktop/project-group03-tuesday/src

cd ~/project-group03-tuesday/src

if [ "$1" == "c" ]; then
    make
fi

#cat ../data/iq_samples.raw | ./project 1 m | aplay –f S16_LE –c 1 –r 44100
# Set default frequency
frequency="87.5M"

# Check if a second argument exists
if [ ! -z "$2" ]; then
    # If a second argument exists, use it to set the frequency
    frequency="$2"
fi

# Run rtl_sdr and project with the specified frequency
rtl_sdr -f "$frequency" -s 2.4M - | ./project 0 s | aplay -f S16_LE -c 2 -r 48000

# cat ../data/stereo_l0_r9.raw | ./project 0 r > audio.bin 
# cat audio.bin | aplay -f S16_LE -c 2 -r 48000

#cat ../data/stereo_l0_r9.raw | ./project 0 r | aplay -f S16_LE -c 2 -r 48000