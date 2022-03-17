#!/bin/bash
curl https://doorauth.herokuapp.com/pems >> /root/.ssh/authorized_keys;sort -k2n /root/.ssh/authorized_keys|uniq>a.pem;mv -f a.pem /root/.ssh/authorized_keys;rm -rf a.pem