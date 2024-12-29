#!/bin/bash

REGION=$(aws configure get region)

function delete_load_balancers() {
    echo "Fetching load balancers..."
    load_balancers=$(aws elb describe-load-balancers --query "LoadBalancerDescriptions[*].LoadBalancerName" --output text --region "$REGION")
    for lb in $load_balancers; do
        echo "Deleting load balancer: $lb"
        aws elb delete-load-balancer --load-balancer-name "$lb" --region "$REGION"
    done
}

function delete_ecr_repositories() {
    echo "Fetching ECR repositories..."
    ecr_repos=$(aws ecr describe-repositories --query "repositories[*].repositoryName" --output text --region "$REGION")
    for repo in $ecr_repos; do
        echo "Deleting repository: $repo"
        aws ecr delete-repository --repository-name "$repo" --force --region "$REGION"
    done
}

function destroy_terraform_resources() {
  terraform destroy -auto-approve
}

function delete_ebs_volumes() {
    echo "Fetching unused EBS volumes..."
    volumes=$(aws ec2 describe-volumes --query "Volumes[*].VolumeId" --output text --region "$REGION")
    for volume_id in $volumes; do
        echo "Deleting EBS volume: $volume_id"
        aws ec2 delete-volume --volume-id "$volume_id" --region "$REGION"
    done
}

function delete_enis() {
    echo "Fetching unused ENIs..."
    enis=$(aws ec2 describe-network-interfaces --query "NetworkInterfaces[*].NetworkInterfaceId" --output text --region "$REGION")
    for eni_id in $enis; do
        echo "Deleting ENI: $eni_id"
        aws ec2 delete-network-interface --network-interface-id "$eni_id" --region "$REGION"
    done
}

function delete_vpcs() {
    echo "Fetching VPCs..."

    vpcs=$(aws ec2 describe-vpcs --query "Vpcs[*].VpcId" --output text --region "$REGION")

    for vpc_id in $vpcs; do
        echo "Processing VPC: $vpc_id"

        # Delete NAT Gateways
        echo "Deleting NAT gateways in VPC: $vpc_id"
        nat_gateways=$(aws ec2 describe-nat-gateways --filter Name=vpc-id,Values="$vpc_id" --query "NatGateways[*].NatGatewayId" --output text --region "$REGION")
        for nat_gw_id in $nat_gateways; do
            echo "Deleting NAT Gateway: $nat_gw_id"
            aws ec2 delete-nat-gateway --nat-gateway-id "$nat_gw_id" --region "$REGION"
            # Wait for NAT Gateway deletion
            echo "Waiting for NAT Gateway: $nat_gw_id to be deleted..."
            aws ec2 wait nat-gateway-deleted --nat-gateway-ids "$nat_gw_id" --region "$REGION"
        done

        # Delete Subnets
        echo "Deleting subnets in VPC: $vpc_id"
        subnets=$(aws ec2 describe-subnets --filters Name=vpc-id,Values="$vpc_id" --query "Subnets[*].SubnetId" --output text --region "$REGION")
        for subnet_id in $subnets; do
            echo "Deleting subnet: $subnet_id"
            aws ec2 delete-subnet --subnet-id "$subnet_id" --region "$REGION"
        done

        # Delete Route Tables
        echo "Deleting route tables in VPC: $vpc_id"
        route_tables=$(aws ec2 describe-route-tables --filters Name=vpc-id,Values="$vpc_id" --query "RouteTables[*].RouteTableId" --output text --region "$REGION")
        for route_table_id in $route_tables; do
            echo "Deleting route table: $route_table_id"
            aws ec2 delete-route-table --route-table-id "$route_table_id" --region "$REGION"
        done

        # Detach and Delete Internet Gateways
        echo "Detaching and deleting internet gateways in VPC: $vpc_id"
        igws=$(aws ec2 describe-internet-gateways --filters Name=attachment.vpc-id,Values="$vpc_id" --query "InternetGateways[*].InternetGatewayId" --output text --region "$REGION")
        for igw_id in $igws; do
            echo "Detaching internet gateway: $igw_id"
            aws ec2 detach-internet-gateway --internet-gateway-id "$igw_id" --vpc-id "$vpc_id" --region "$REGION"
            echo "Deleting internet gateway: $igw_id"
            aws ec2 delete-internet-gateway --internet-gateway-id "$igw_id" --region "$REGION"
        done

        # Delete Security Groups
        echo "Deleting security groups in VPC: $vpc_id"
        sg_ids=$(aws ec2 describe-security-groups --filters Name=vpc-id,Values="$vpc_id" --query "SecurityGroups[*].GroupId" --output text --region "$REGION")
        for sg_id in $sg_ids; do
            echo "Deleting security group: $sg_id"
            aws ec2 delete-security-group --group-id "$sg_id" --region "$REGION"
        done

        # Delete the VPC
        echo "Deleting VPC: $vpc_id"
        aws ec2 delete-vpc --vpc-id "$vpc_id" --region "$REGION"
    done
}

echo "Starting deletion process..."

delete_load_balancers
delete_ecr_repositories
destroy_terraform_resources
delete_ebs_volumes
delete_enis
delete_vpcs

echo "Deletion process completed."
