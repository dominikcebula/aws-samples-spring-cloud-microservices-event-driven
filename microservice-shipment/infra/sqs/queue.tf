resource "aws_sqs_queue" "customer_events_to_shipment_service" {
  name = "customer-events-to-shipment-service"
}

resource "aws_sns_topic_subscription" "customer_events_to_shipment_service_subscription" {
  topic_arn = var.customer_events_topic_arn
  protocol  = "sqs"
  endpoint  = aws_sqs_queue.customer_events_to_shipment_service.arn
}
