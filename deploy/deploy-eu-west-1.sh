#!/bin/bash

home=../
scriptdir=eu-west-1
pemfile=odin-west-1.pem
dest=ubuntu@9.10.11.12:~/.

scp -i ${pemfile} ${home}/burpabet-betting-service/target/betting-service.jar ${dest}
scp -i ${pemfile} ${home}/burpabet-customer-service/target/customer-service.jar ${dest}
scp -i ${pemfile} ${home}/burpabet-wallet-service/target/wallet-service.jar ${dest}
scp -i ${pemfile} ${scriptdir}/*.sh ${dest}
scp -i ${pemfile} ${scriptdir}/*.sql ${dest}