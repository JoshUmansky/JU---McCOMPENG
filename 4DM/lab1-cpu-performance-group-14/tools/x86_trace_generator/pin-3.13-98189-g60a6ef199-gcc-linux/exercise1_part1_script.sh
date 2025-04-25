#!/bin/sh
 # This is a comment!

 # First delete all existing raw files to regenerate clean:
find $1 -name "*.raw" -type f -delete

for target in $(find $1 -type f ! -size 0 -exec grep -IL . "{}" \;);
do
   filename="${target##*/}"  # Get filename
   dirname="${target%/*}" # Get directory/path name
   echo "..........."
   echo $filename
   echo $target
   ./pin -t /home/gp14/lab1-cpu-performance-group-14/tools/x86_trace_generator/pin-3.13-98189-g60a6ef199-gcc-linux/source/tools/ManualExamples/obj-intel64/inscount0.so -o ${dirname}/${filename}_inscount -- $target
done
