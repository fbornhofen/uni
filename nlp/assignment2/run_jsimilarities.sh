#!/bin/bash

cd JSimilarities/bin
time java -Xmx4096m de.bfabian.similarities.Main ../../data/5000lines.txt context.txt word-pairs.txt 4 1 500