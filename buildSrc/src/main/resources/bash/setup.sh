#!/bin/bash

CUR_DIR=$(pwd)

rm -fR svn-test
mkdir -p svn-test/repository/testrepo
mkdir -p svn-test/checkout
svnadmin create svn-test/repository/testrepo

cd svn-test/checkout
svn checkout "file:///$CUR_DIR/svn-test/repository/testrepo" testrepo > /dev/null

cd "$CUR_DIR" || return
