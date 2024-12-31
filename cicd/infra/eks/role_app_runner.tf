resource "aws_iam_role" "app_runner" {
  name = "app-runner"

  assume_role_policy = data.aws_iam_policy_document.app_runner_assume_role_policy.json
}

data "aws_iam_policy_document" "app_runner_assume_role_policy" {
  statement {
    effect = "Allow"
    actions = ["sts:AssumeRole"]
    principals {
      type = "Service"
      identifiers = ["eks.amazonaws.com"]
    }
  }
}
