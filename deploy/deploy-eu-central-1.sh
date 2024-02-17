#!/bin/bash

home=../
scriptdir=eu-central-1
pemfile=odin-central-1.pem
dest=ubuntu@1.2.3.4:~/.

scp -i ${pemfile} ${home}/burpabet-betting-service/target/betting-service.jar ${dest}
scp -i ${pemfile} ${home}/burpabet-customer-service/target/customer-service.jar ${dest}
scp -i ${pemfile} ${home}/burpabet-wallet-service/target/wallet-service.jar ${dest}
scp -i ${pemfile} ${scriptdir}/*.sh ${dest}
scp -i ${pemfile} ${scriptdir}/*.sql ${dest}
