#!/bin/sh

set -e

java -cp cyoencrypt.jar cyoencrypt/Encrypt $1 $2 $3 $4
