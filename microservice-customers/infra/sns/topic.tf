resource "aws_sns_topic" "customer_events_topic" {
  name = "customer-events-topic"
}

resource "aws_sns_topic_policy" "customer_events_topic_policy" {
  arn    = aws_sns_topic.customer_events_topic.arn
  policy = data.aws_iam_policy_document.customer_events_topic_policy_document.json
}

data "aws_caller_identity" "current" {}

data "aws_iam_policy_document" "customer_events_topic_policy_document" {
  policy_id = "__default_policy_ID"

  statement {
    sid = "__default_statement_ID"

    effect = "Allow"

    principals {
      identifiers = [data.aws_caller_identity.current.arn]
      type = "AWS"
    }

    actions = [
      "SNS:GetTopicAttributes",
      "SNS:SetTopicAttributes",
      "SNS:AddPermission",
      "SNS:RemovePermission",
      "SNS:DeleteTopic",
      "SNS:Subscribe",
      "SNS:ListSubscriptionsByTopic",
      "SNS:Publish"
    ]
    resources = [aws_sns_topic.customer_events_topic.arn]
  }

  statement {
    sid = "allow-app-to-publish-messages"

    effect = "Allow"

    actions = [
      "SNS:Publish",
    ]

    principals {
      type = "AWS"
      identifiers = [var.service_account_arn]
    }

    resources = [aws_sns_topic.customer_events_topic.arn]
  }
}
