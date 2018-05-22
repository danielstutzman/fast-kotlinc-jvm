#!/bin/bash -ex
KOTLINC=~/dev/kotlinc/bin/kotlinc-jvm
$KOTLINC -d output-expected input/*.kt
