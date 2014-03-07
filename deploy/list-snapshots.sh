#!/bin/bash
. lib.sh
check-installs
aws ec2 describe-snapshots --filter "Name=description,Values=Incremental backup for DCUtilVol"  | jq '.Snapshots[] | {StartTime, SnapshotId}'