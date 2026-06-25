#!/bin/bash
#
#  Copyright (C) Timothy Baxendale
# 
#  This library is free software; you can redistribute it and/or
#  modify it under the terms of the GNU Lesser General Public
#  License as published by the Free Software Foundation; either
#  version 2.1 of the License, or (at your option) any later version.
# 
#  This library is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
#  Lesser General Public License for more details.
# 
#  You should have received a copy of the GNU Lesser General Public
#  License along with this library; if not, write to the Free Software
#  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
#  USA
#

if [[ -z "$JAVA_HOME" ]]; then
    export JAVA_HOME="$(readlink -f /etc/alternatives/javac)"
    export JAVA_HOME="${JAVA_HOME%/*}"
    export JAVA_HOME="${JAVA_HOME%/*}"
fi

SHOPS_VER="1.0b4"
VERSION='snapshot'

printf 'JAVA_HOME= %s\n' "${JAVA_HOME}"
printf 'SHOPS_VER= %s\n' "${SHOPS_VER}"

compile() {    
    printf 'Building version %s...\n' "${VERSION}"
    
    mvn install clean
    STATUS=$?
    if [[ $STATUS = 0 ]]; then
        if mvn package; then
            if [[ ! -d './bin' ]]; then
                mkdir './bin'
            fi
            cp -v "./target/baxshops-${SHOPS_VER}-SNAPSHOT.jar" "./bin/baxshops-$SHOPS_VER-SNAPSHOT.jar"
        fi
    fi
    
    printf 'Done.\n'
    return $STATUS
}

compile
exit $?
