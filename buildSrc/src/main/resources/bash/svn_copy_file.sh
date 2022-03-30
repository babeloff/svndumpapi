#!/bin/bash -e

SVNFLAGS="-q"

CURDIR=$(pwd)
SCRIPT_DIR=$(cd $(dirname $0) && pwd)
cd "$SCRIPT_DIR" || return
source ./setup.sh

cd svn-test/checkout/testrepo

echo "this is a test file" > README.txt
svn add $SVNFLAGS README.txt
svn commit $SVNFLAGS -m "Added readme."
svn cp $SVNFLAGS README.txt OTHER.txt
svn commit $SVNFLAGS -m "Copied readme."

cd "$SCRIPT_DIR" || return
source ./export.sh
cd "$CURDIR" || return
