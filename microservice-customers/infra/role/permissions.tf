data "aws_caller_identity" "current" {}
data "aws_region" "current" {}

resource "aws_iam_role_policy" "test_policy" {
  name = "SnsPolicyForCustomersMicroservice"
  role = var.service_account_id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = [
          "SNS:Publish",
          "SNS:CreateTopic"
        ]
        Effect   = "Allow"
        Resource = var.customer_events_topic_arn
      },
      {
        Action = [
          "SNS:ListTopics",
        ]
        Effect   = "Allow"
        Resource = "arn:aws:sns:${data.aws_region.current.name}:${data.aws_caller_identity.current.account_id}:*"
      },
    ]
  })
}