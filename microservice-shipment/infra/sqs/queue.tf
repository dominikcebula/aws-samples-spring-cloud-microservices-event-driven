resource "aws_sqs_queue" "customer_events_to_shipment_service" {
  name = "customer-events-to-shipment-service"
}

resource "aws_sns_topic_subscription" "customer_events_to_shipment_service_subscription" {
  topic_arn = var.customer_events_topic_arn
  protocol  = "sqs"
  endpoint  = aws_sqs_queue.customer_events_to_shipment_service.arn
}

resource "aws_sqs_queue_policy" "sqs_policy" {
  queue_url = aws_sqs_queue.customer_events_to_shipment_service.url
  policy    = data.aws_iam_policy_document.sqs_policy_document.json
}

data "aws_caller_identity" "current" {}

data "aws_iam_policy_document" "sqs_policy_document" {
  statement {
    sid = "__owner_statement"

    effect = "Allow"

    principals {
      identifiers = [data.aws_caller_identity.current.arn]
      type = "AWS"
    }

    actions = ["SQS:*"]
    resources = [aws_sqs_queue.customer_events_to_shipment_service.arn]
  }

  statement {
    sid = "topic-subscription-arn:${var.customer_events_topic_arn}"

    effect = "Allow"

    principals {
      identifiers = ["*"]
      type = "AWS"
    }

    actions = ["SQS:SendMessage"]

    resources = [aws_sqs_queue.customer_events_to_shipment_service.arn]

    condition {
      test     = "ArnLike"
      values = [var.customer_events_topic_arn]
      variable = "aws:SourceArn"
    }
  }
}
