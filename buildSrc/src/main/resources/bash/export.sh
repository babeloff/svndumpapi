#!/bin/bash

svnadmin dump -q --deltas svn-test/repository/testrepo
rm -fR svn-test
