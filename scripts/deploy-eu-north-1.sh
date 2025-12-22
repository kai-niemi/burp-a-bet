#!/bin/bash

home=../
scriptdir=eu-north-1
pemfile=odin-north-1.pem
dest=ubuntu@5.6.7.8:~/.

scp -i ${pemfile} ${home}/betting-service/target/betting-service.jar ${dest}
scp -i ${pemfile} ${home}/customer-service/target/customer-service.jar ${dest}
scp -i ${pemfile} ${home}/wallet-service/target/wallet-service.jar ${dest}
scp -i ${pemfile} ${scriptdir}/*.sh ${dest}
scp -i ${pemfile} ${scriptdir}/*.sql ${dest}