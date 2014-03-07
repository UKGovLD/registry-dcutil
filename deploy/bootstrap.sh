#!/bin/bash
set -e

RELEASE=snapshot/com/github/ukgovlds/registry-dcutil/0.0.1-SNAPSHOT/registry-dcutil-0.0.1-20140307.170653-4.war

echo "** General updates"
yum update -y
yum install -y curl chkconfig

. /vagrant/install-nginx.sh
. /vagrant/install-java.sh
. /vagrant/mount-ebs.sh

# Configure runtime areas
if ! blkid | grep /dev/xvdf; then
  # If it's snapshot the ldregistry areas will exist, otherwise create them
  if [ ! -d /mnt/varopt/dcutil ]; then
    mkdir -p /mnt/varopt/dcutil
    chown tomcat7 /mnt/varopt/dcutil
  fi
  if [ ! -d /var/opt/dcutil ]; then
    ln -s /mnt/varopt/dcutil /var/opt
  fi
else
  # No attached volume, just just create empty ldregistry areas
  if [ ! -d "/var/opt/dcutil" ]; then
    mkdir /var/opt/dcutil
    chown tomcat /var/opt/dcutil
  fi
fi

# Set up configuration area /opt/ldregistry
echo "** Installing registry-util application"
rm -rf /var/lib/tomcat7/webapps/ROOT* || true
rm -rf /var/lib/tomcat7/webapps/dcutil* || true
rm -rf /var/lib/tomcat7/webapps/registry-util  || true
curl -4s https://s3-eu-west-1.amazonaws.com/ukgovld/$RELEASE > /var/lib/tomcat7/webapps/registry-util.war



