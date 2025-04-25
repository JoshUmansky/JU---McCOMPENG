#!/bin/bash

#cd ~/Desktop/project-group03-tuesday/src

cd ~/project-group03-tuesday/src

#make

cat ../data/iq_samples.raw | ./project 0 m | aplay -f S16_LE -c 1 -r 48000

cat ../data/iq_samples1152.raw | ./project 1 m | aplay -f S16_LE -c 1 -r 32000

cat ../data/iq_samples.raw | ./project 2 m | aplay -f S16_LE -c 1 -r 44100

cat ../data/iq_samples144.raw | ./project 3 m | aplay -f S16_LE -c 1 -r 44100


cat ../data/stereo_l0_r9.raw | ./project 0 s | aplay -f S16_LE -c 2 -r 48000

cat ../data/stereo1152.raw | ./project 1 s | aplay -f S16_LE -c 2 -r 32000

cat ../data/stereo_l0_r9.raw | ./project 2 s | aplay -f S16_LE -c 2 -r 44100

cat ../data/stereo144.raw | ./project 3 s | aplay -f S16_LE -c 2 -r 44100


cat ../data/stereo_l0_r9.raw | ./project 0 r | aplay -f S16_LE -c 2 -r 48000


cat ../data/stereo_l0_r9.raw | ./project 2 r | aplay -f S16_LE -c 2 -r 44100