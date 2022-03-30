#!/bin/bash -e

SVNFLAGS="-q"

CURDIR=$(pwd)
SCRIPT_DIR=$(cd $(dirname $0) && pwd)
cd "$SCRIPT_DIR" || return
source ./setup.sh

cd svn-test/checkout/testrepo

svn propset $SVNFLAGS customproperty myval .
svn commit $SVNFLAGS -m "Setting property on the root."

cd "$SCRIPT_DIR" || return
source ./export.sh
cd "$CURDIR" || return
