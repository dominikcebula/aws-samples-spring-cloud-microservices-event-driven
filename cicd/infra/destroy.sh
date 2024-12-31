#!/bin/bash

# Bash helper script to destroy all resources on AWS account created by this project.
# It was implemented because for some reason Terraform has issues when deleting VPCs,
# deletion is blocked by ENIs used by LBs. Additionally Terraform cannot delete ECRs even with force_delete.

REGION=$(aws configure get region)
AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query "Account" --output text --no-cli-pager)

function delete_eks_node_groups() {
    echo "Fetching EKS clusters for node groups list..."
    eks_clusters=$(aws eks list-clusters --query "clusters[*]" --output text --region "$REGION" --no-cli-pager)
    for cluster in $eks_clusters; do
        echo "Fetching EKS node groups for cluster: $cluster"
        node_groups=$(aws eks list-nodegroups --cluster-name "$cluster" --query "nodegroups[*]" --output text --region "$REGION" --no-cli-pager)
        for node_group in $node_groups; do
            echo "Deleting EKS node group: $node_group in cluster: $cluster"
            aws eks delete-nodegroup --cluster-name "$cluster" --nodegroup-name "$node_group" --output text --region "$REGION" --no-cli-pager
            # Wait for EKS node group deletion
            echo "Waiting for EKS node group: $node_group in cluster: $cluster to be deleted..."
            aws eks wait nodegroup-deleted --cluster-name "$cluster" --nodegroup-name "$node_group" --region "$REGION" --no-cli-pager
        done
    done
}

function delete_eks_clusters() {
    echo "Fetching EKS clusters..."
    eks_clusters=$(aws eks list-clusters --query "clusters[*]" --output text --region "$REGION" --no-cli-pager)
    for cluster in $eks_clusters; do
        echo "Deleting EKS cluster: $cluster"
        aws eks delete-cluster --name "$cluster" --output text --region "$REGION" --no-cli-pager
        # Wait for EKS cluster deletion
        echo "Waiting for EKS cluster: $cluster to be deleted..."
        aws eks wait cluster-deleted --name "$cluster" --region "$REGION" --no-cli-pager
    done
}

function delete_ec2_instances() {
    echo "Fetching EC2 instances..."
    instances=$(aws ec2 describe-instances --query "Reservations[*].Instances[*].InstanceId" --output text --region "$REGION" --no-cli-pager)
    for instance_id in $instances; do
        echo "Terminating EC2 instance: $instance_id"
        aws ec2 terminate-instances --instance-ids "$instance_id" --output text --region "$REGION" --no-cli-pager
    done
    # Wait for EC2 instances to be terminated
    echo "Waiting for EC2 instances to be terminated..."
    aws ec2 wait instance-terminated --instance-ids "$instances" --region "$REGION" --no-cli-pager
}

function delete_load_balancers() {
    echo "Fetching load balancers..."
    load_balancers=$(aws elb describe-load-balancers --query "LoadBalancerDescriptions[*].LoadBalancerName" --output text --region "$REGION" --no-cli-pager)
    for lb in $load_balancers; do
        echo "Deleting load balancer: $lb"
        aws elb delete-load-balancer --load-balancer-name "$lb" --region "$REGION" --output text --no-cli-pager
    done
}

function delete_ecr_repositories() {
    echo "Fetching ECR repositories..."
    ecr_repos=$(aws ecr describe-repositories --query "repositories[*].repositoryName" --output text --region "$REGION" --no-cli-pager)
    for repo in $ecr_repos; do
        echo "Deleting repository: $repo"
        aws ecr delete-repository --repository-name "$repo" --force --region "$REGION" --output text --no-cli-pager
    done
}

function delete_rds_resources() {
    echo "Fetching RDS instances..."
    rds_instances=$(aws rds describe-db-instances --query "DBInstances[*].DBInstanceIdentifier" --output text --region "$REGION" --no-cli-pager)
    for instance_id in $rds_instances; do
        echo "Deleting RDS instance: $instance_id"
        aws rds delete-db-instance --db-instance-identifier "$instance_id" --skip-final-snapshot --region "$REGION" --output text --no-cli-pager
        echo "Waiting for RDS instance: $instance_id to be deleted..."
        aws rds wait db-instance-deleted --db-instance-identifier "$instance_id" --region "$REGION" --no-cli-pager
    done

    echo "Fetching RDS clusters..."
    rds_clusters=$(aws rds describe-db-clusters --query "DBClusters[*].DBClusterIdentifier" --output text --region "$REGION" --no-cli-pager)
    for cluster_id in $rds_clusters; do
        echo "Deleting RDS cluster: $cluster_id"
        aws rds delete-db-cluster --db-cluster-identifier "$cluster_id" --skip-final-snapshot --region "$REGION" --output text --no-cli-pager
        echo "Waiting for RDS cluster: $cluster_id to be deleted..."
        aws rds wait db-cluster-deleted --db-cluster-identifier "$cluster_id" --region "$REGION" --no-cli-pager
    done

    echo "Fetching RDS snapshots..."
    rds_snapshots=$(aws rds describe-db-snapshots --query "DBSnapshots[*].DBSnapshotIdentifier" --output text --region "$REGION" --no-cli-pager)
    for snapshot_id in $rds_snapshots; do
        echo "Deleting RDS snapshot: $snapshot_id"
        aws rds delete-db-snapshot --db-snapshot-identifier "$snapshot_id" --region "$REGION" --output text --no-cli-pager
    done

    echo "Fetching RDS subnet groups..."
    rds_subnet_groups=$(aws rds describe-db-subnet-groups --query "DBSubnetGroups[*].DBSubnetGroupName" --output text --region "$REGION" --no-cli-pager)
    for subnet_group in $rds_subnet_groups; do
        echo "Deleting RDS subnet group: $subnet_group"
        aws rds delete-db-subnet-group --db-subnet-group-name "$subnet_group" --region "$REGION" --output text --no-cli-pager
    done
}

function delete_ebs_volumes() {
    # Delete EBS Volumes
    echo "Fetching EBS volumes..."
    volumes=$(aws ec2 describe-volumes --query "Volumes[*].VolumeId" --output text --region "$REGION" --no-cli-pager)
    for volume_id in $volumes; do
        echo "Deleting EBS volume: $volume_id"
        aws ec2 delete-volume --volume-id "$volume_id" --region "$REGION" --output text --no-cli-pager
    done

    # Delete EBS Snapshots
    echo "Fetching EBS snapshots..."
    snapshots=$(aws ec2 describe-snapshots --filters Name=owner-id,Values="${AWS_ACCOUNT_ID}" --query "Snapshots[*].SnapshotId" --output text --region "$REGION" --no-cli-pager)
    for snapshot_id in $snapshots; do
        echo "Deleting EBS snapshot: $snapshot_id"
        aws ec2 delete-snapshot --snapshot-id "$snapshot_id" --region "$REGION" --output text --no-cli-pager
    done
}

function delete_enis() {
    echo "Fetching unused ENIs..."
    enis=$(aws ec2 describe-network-interfaces --query "NetworkInterfaces[*].NetworkInterfaceId" --output text --region "$REGION" --no-cli-pager)
    for eni_id in $enis; do
        echo "Deleting ENI: $eni_id"
        aws ec2 delete-network-interface --network-interface-id "$eni_id" --region "$REGION" --output text --no-cli-pager
    done
}

function delete_vpcs() {
    echo "Fetching VPCs..."

    vpcs=$(aws ec2 describe-vpcs --query "Vpcs[*].VpcId" --output text --region "$REGION" --no-cli-pager)

    for vpc_id in $vpcs; do
        echo "Processing VPC: $vpc_id"

        # Delete NAT Gateways
        echo "Deleting NAT gateways in VPC: $vpc_id"
        nat_gateways=$(aws ec2 describe-nat-gateways --filter Name=vpc-id,Values="$vpc_id" --query "NatGateways[*].NatGatewayId" --output text --region "$REGION" --no-cli-pager)
        for nat_gw_id in $nat_gateways; do
            echo "Deleting NAT Gateway: $nat_gw_id"
            aws ec2 delete-nat-gateway --nat-gateway-id "$nat_gw_id" --region "$REGION" --output text --no-cli-pager
            # Wait for NAT Gateway deletion
            echo "Waiting for NAT Gateway: $nat_gw_id to be deleted..."
            aws ec2 wait nat-gateway-deleted --nat-gateway-ids "$nat_gw_id" --region "$REGION" --no-cli-pager
        done

        # Delete Subnets
        echo "Deleting subnets in VPC: $vpc_id"
        subnets=$(aws ec2 describe-subnets --filters Name=vpc-id,Values="$vpc_id" --query "Subnets[*].SubnetId" --output text --region "$REGION" --no-cli-pager)
        for subnet_id in $subnets; do
            echo "Deleting subnet: $subnet_id"
            aws ec2 delete-subnet --subnet-id "$subnet_id" --region "$REGION" --output text --no-cli-pager
        done

        # Delete Route Tables
        echo "Deleting route tables in VPC: $vpc_id"
        route_tables=$(aws ec2 describe-route-tables --filters Name=vpc-id,Values="$vpc_id" --query "RouteTables[*].RouteTableId" --output text --region "$REGION" --no-cli-pager)
        for route_table_id in $route_tables; do
            echo "Deleting route table: $route_table_id"
            aws ec2 delete-route-table --route-table-id "$route_table_id" --region "$REGION" --output text --no-cli-pager
        done

        # Detach and Delete Internet Gateways
        echo "Detaching and deleting internet gateways in VPC: $vpc_id"
        igws=$(aws ec2 describe-internet-gateways --filters Name=attachment.vpc-id,Values="$vpc_id" --query "InternetGateways[*].InternetGatewayId" --output text --region "$REGION" --no-cli-pager)
        for igw_id in $igws; do
            echo "Detaching internet gateway: $igw_id"
            aws ec2 detach-internet-gateway --internet-gateway-id "$igw_id" --vpc-id "$vpc_id" --region "$REGION" --output text --no-cli-pager
            echo "Deleting internet gateway: $igw_id"
            aws ec2 delete-internet-gateway --internet-gateway-id "$igw_id" --region "$REGION" --output text --no-cli-pager
        done

        # Delete Security Groups
        echo "Deleting security groups in VPC: $vpc_id"
        sg_ids=$(aws ec2 describe-security-groups --filters Name=vpc-id,Values="$vpc_id" --query "SecurityGroups[*].GroupId" --output text --region "$REGION" --no-cli-pager)
        for sg_id in $sg_ids; do
            echo "Deleting security group: $sg_id"
            aws ec2 delete-security-group --group-id "$sg_id" --region "$REGION" --output text --no-cli-pager
        done

        # Delete Network ACLs
        echo "Deleting network ACLs in VPC: $vpc_id"
        nacl_ids=$(aws ec2 describe-network-acls --filters Name=vpc-id,Values="$vpc_id" --query "NetworkAcls[*].NetworkAclId" --output text --region "$REGION" --no-cli-pager)
        for nacl_id in $nacl_ids; do
            echo "Deleting network ACL: $nacl_id"
            aws ec2 delete-network-acl --network-acl-id "$nacl_id" --region "$REGION" --output text --no-cli-pager
        done

        # Delete DHCP option sets
        echo "Deleting DHCP option sets in VPC: $vpc_id"
        dhcp_option_sets=$(aws ec2 describe-dhcp-options --query "DhcpOptions[*].DhcpOptionsId" --output text --region "$REGION" --no-cli-pager)
        for dhcp_option_set_id in $dhcp_option_sets; do
            echo "Deleting DHCP option set: $dhcp_option_set_id"
            aws ec2 delete-dhcp-options --dhcp-options-id "$dhcp_option_set_id" --region "$REGION" --output text --no-cli-pager
        done

        # Delete Elastic IPs
        echo "Deleting Elastic IPs in VPC: $vpc_id"
        eips=$(aws ec2 describe-addresses --query "Addresses[*].AllocationId" --output text --region "$REGION" --no-cli-pager)
        for eip_id in $eips; do
            echo "Releasing Elastic IP: $eip_id"
            aws ec2 release-address --allocation-id "$eip_id" --region "$REGION" --output text --no-cli-pager
        done

        # Delete the VPC
        echo "Deleting VPC: $vpc_id"
        aws ec2 delete-vpc --vpc-id "$vpc_id" --region "$REGION" --output text --no-cli-pager
    done
}

function destroy_terraform_resources() {
  terraform destroy -auto-approve
}

echo "Starting deletion process..."

delete_eks_node_groups
delete_eks_clusters
delete_ec2_instances
delete_load_balancers
delete_ecr_repositories
delete_rds_resources
delete_ebs_volumes
delete_enis
delete_vpcs
destroy_terraform_resources

echo "Deletion process completed."