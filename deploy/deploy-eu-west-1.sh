#!/bin/bash

home=../
scriptdir=eu-west-1
pemfile=odin-west-1.pem
dest=ubuntu@9.10.11.12:~/.

scp -i ${pemfile} ${home}/betting-service/target/betting-service.jar ${dest}
scp -i ${pemfile} ${home}/customer-service/target/customer-service.jar ${dest}
scp -i ${pemfile} ${home}/wallet-service/target/wallet-service.jar ${dest}
scp -i ${pemfile} ${scriptdir}/*.sh ${dest}
scp -i ${pemfile} ${scriptdir}/*.sql ${dest}