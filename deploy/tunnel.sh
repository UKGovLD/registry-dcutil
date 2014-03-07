#!/bin/bash
# usage:  tunnel [port]
# default port is 80

PORT=${1:-80}

INSTANCE=`cat .vagrant/machines/default/aws/id`
[[ -n $INSTANCE ]] || { echo "Can't find instance to attach to" 1>&2; exit 1; }
IP=`aws ec2 describe-instances --instance-id $INSTANCE | jq .Reservations[0].Instances[0].PublicIpAddress | sed -e 's/"\(.*\)"/\1/'`
[[ -n $IP ]] || { echo "Can't find instance IP" 1>&2; exit 1; }

ssh -4 -i $AWS_SSH_PRIVKEY -f ec2-user@$IP -L $PORT:localhost:$9080 -N