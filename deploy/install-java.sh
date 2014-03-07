# bootstreap fragment for installing java and tomcat
# Assumes Amazon Linux 

echo "** Installing java and tomcat ..."
yum install -y java-1.7.0-openjdk-demo.x86_64 tomcat7.noarch
alternatives --set java /usr/lib/jvm/jre-1.7.0-openjdk.x86_64/bin/java

if [ $(java -version 2>&1 | grep 1.7. -c) -ne 1 ]
then
  echo "**   ERROR: java version doesn't look right, try manual alternatives setting restart tomcat7"
  echo "**   java version is:"
  java -version
fi

echo "**    starting tomcat"
service tomcat7 start
chkconfig tomcat7 on
