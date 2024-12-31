locals {
  app_runner_role_name = "app-runner"
}

resource "aws_iam_role" "app_runner" {
  name = local.app_runner_role_name

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

  statement {
    effect = "Allow"
    actions = ["sts:AssumeRoleWithWebIdentity"]
    principals {
      type = "Federated"
      identifiers = [
        "arn:aws:iam::${var.aws_account_id}:oidc-provider/${module.eks.oidc_provider}"
      ]
    }
    condition {
      test     = "StringEquals"
      variable = "${module.eks.oidc_provider}:sub"
      values = ["system:serviceaccount:default:${local.app_runner_role_name}"]
    }
    condition {
      test     = "StringEquals"
      variable = "${module.eks.oidc_provider}:aud"
      values = ["sts.amazonaws.com"]
    }
  }
}
