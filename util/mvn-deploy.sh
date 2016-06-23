#!/bin/bash
keys="$(gpg --list-keys | grep ^pub | sed 's#/# #' | awk '{ print $3 }')"
key_count="$(echo ${keys} | wc -w)"

seen=""
while [[ $# > 0 ]] ; do
  param="$1"
  if [[ $param == "--signing-key" ]]; then
    # disambiguating or overriding key
    key="$2"
    shift
  else
    seen="${seen} ${param}"
  fi
  shift
done
params=${seen}

if [[ ${key_count} -lt 1 ]]; then
  echo ""
  echo "You are attempting to deploy a maven release without a GPG signing key."
  echo "You need to generate a signing key in accordance with the instructions"
  echo "found at http://blog.sonatype.com/2010/01/how-to-generate-pgp-signatures-with-maven"
  exit 1
fi

# if a key is specified, use that, else use the default, unless there are many
if [[ -n "${key}" ]]; then
  #validate key
  keystatus=$(gpg --list-keys | grep ${key} | awk '{print $1}')
  if [ "${keystatus}" != "pub" ]; then
    echo ""
    echo "Could not find public key with label \"${key}\""
    echo ""
    echo "Available keys from: "
    gpg --list-keys | grep --invert-match '^sub'
    exit 1
  fi

  key_param="-Dgpg.keyname=${key}"
elif [ ${key_count} -gt 1 ]; then
  echo ""
  echo "You are attempting to deploy a maven release but have more than one GPG"
  echo "signing key and did not specify which one you wish to sign with."
  echo ""
  echo "usage $0 [--signing-key <ssl-key>] [<maven params> ...]"
  echo ""
  echo -n "Available keys from: "
  gpg --list-keys | grep --invert-match '^sub'
  exit 1;
fi

mvn ${params} clean site:jar -P sonatype-oss-release ${key_param} deploy
