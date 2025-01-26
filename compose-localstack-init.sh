#!/bin/bash

echo "Creating SNS and SQS Topics..."

awslocal sns create-topic --name customer-events-topic
awslocal sqs create-queue --queue-name customer-events-to-shipment-service
awslocal sns subscribe --topic-arn arn:aws:sns:us-east-1:000000000000:customer-events-topic \
                          --protocol sqs \
                          --notification-endpoint arn:aws:sqs:us-east-1:000000000000:customer-events-to-shipment-service

echo "Finished creating SNS and SQS Topics."
