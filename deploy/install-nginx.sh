# boostrap fragment for nginx
# Assumes Amazon Linux 

echo "** Installing nginx ..."
echo "**   download package ..."
yum install -y nginx.x86_64

if [ $(grep -c nginx /etc/logrotate.conf) -ne 0 ]
then
  echo "**   logrotate for nginx already configured"
else
  cat /vagrant/install/nginx.logrotate.conf >> /etc/logrotate.conf
  echo "**   logrotate for nginx configured"
fi
cp /vagrant/install/nginx.conf /etc/nginx/conf.d/localhost.conf

echo "**   starting service ..."
service nginx start
chkconfig nginx on
