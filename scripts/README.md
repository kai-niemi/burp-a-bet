# Multi-Region AWS Deployment (Advanced)

This directory provides template script files for manually deploying the 
services to a multi-region cluster on AWS across the following regions:

- aws-eu-west-1 (Ireland)
- aws-eu-north-1 (Sweden)
- aws-eu-central-1 (Germany)

There are also `*-multi-region.sql` files for each database that will
configure table localities and partitions based on the [Jurisdiction](../burpabet-common/src/main/java/io/burpabet/common/domain/Jurisdiction.java)
enum type.

Note that its templates that will need editing to match the CockroachCloud cluster 
and VPCs you use for the services, including Kafka.

## Overall Steps

It's advised to use separate regional kafka clusters and the geo-filter
the outbox table changefeeds accordingly by adding a region predicate. This
is not included in the default create SQL scripts.

    .. WHERE crdb_region = 'aws-eu-west-1'

The overall steps include:

1. Create a CockroachCloud cluster with 3 nodes in each region
   - eu-north-1 (primary)
   - eu-west-1
   - eu-central-1
2. Create one EC2 client instance in each region (4vcpu minimum)
   - Copy the `.pem` files to this directory for SSH auth
2. Configure networking so the clients can connect via JDBC and that CRDB CDC can reach each local Kafka instance
    - Ensure you assign a public IP and have an igw route in the VPC (assuming no PrivateLink)
    - Whitelisting / PrivateLink: https://www.cockroachlabs.com/docs/cockroachcloud/network-authorization#ip-allowlisting
2. Edit the `**/*.sh` files to match the host names and IPs of your deployment
3. Deploy service JARs to each client using `deploy-*.sh`
2. SSH to each client VM
   - Download the CockroachCloud cluster certificate and test connectivity
   - Install Java 21+ on each client instance (see top README)
   - Install Kafka 3.6+ on each client instance (see top README)
     - Configure `listeners` and `advertised.listeners` addresses so both the services and CockroachDB can connect. 
   - Start the Kafka servers (see top README)
   - Start the application services in each region and let it rip (see top README)
2. (Optional) Apply the geo-partitioning SQL files, let things re-balance and settle 
and watch the performance increase.
   - `cockroach sql --url "betting-db connection string" < betting-multi-region.sql`
   - `cockroach sql --url "customer-db connection string" < customer-multi-region.sql`
   - `cockroach sql --url "wallet-db connection string" < wallet-multi-region.sql`
