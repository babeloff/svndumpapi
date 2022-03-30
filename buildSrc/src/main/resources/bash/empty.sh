#!/bin/bash
CURDIR=`pwd`
cd $(dirname $0)
source ./setup.sh
source ./export.sh
cd "$CURDIR" || return

