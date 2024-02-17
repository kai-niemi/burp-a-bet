#!/bin/bash

home=../
scriptdir=eu-north-1
pemfile=odin-north-1.pem
dest=ubuntu@5.6.7.8:~/.

scp -i ${pemfile} ${home}/burpabet-betting-service/target/betting-service.jar ${dest}
scp -i ${pemfile} ${home}/burpabet-customer-service/target/customer-service.jar ${dest}
scp -i ${pemfile} ${home}/burpabet-wallet-service/target/wallet-service.jar ${dest}
scp -i ${pemfile} ${scriptdir}/*.sh ${dest}
scp -i ${pemfile} ${scriptdir}/*.sql ${dest}