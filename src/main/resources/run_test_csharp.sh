export MONO_GC_PARAMS="nursery-size=256m,major-heap-size=256m"
/usr/bin/time -v mono $1 < input.txt > output.txt 2> metadata.txt
