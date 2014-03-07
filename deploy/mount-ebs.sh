# bootstreap fragment for formatting/mouting an EBS volume if present

# Configure runtime areas
if ! blkid | grep /dev/xvdf; then
  # No visible attached volume but might not be formatted yet
  # This will just fail on a local vm
  mkfs -t ext4 /dev/xvdf || true
fi

if blkid | grep /dev/xvdf; then
  # We have an attached volume, mount it
  if ! mount | grep -q /dev/xvdf ; then
    # Not mounted yet but check if e.g. instance store is mounted in its place
    if mount | grep -q /mnt ; then
      # Yes, in that case remove that mount
      umount /mnt
      mkdir /instance
      sed -i -e 's!/mnt!/instance!g' /etc/fstab
      mount /instance
    fi

    echo "/dev/xvdf /mnt ext4 rw 0 0" | tee -a /etc/fstab > /dev/null && mount /mnt  
  fi
fi