locals {
  service_account_role_name = var.service_account_name
  service_account_sa_name   = "${var.service_account_name}-sa"
}

resource "aws_iam_role" "service_account_role" {
  name = local.service_account_role_name

  assume_role_policy = data.aws_iam_policy_document.service_account_role_policy.json
}

data "aws_iam_policy_document" "service_account_role_policy" {
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
        "arn:aws:iam::${var.aws_account_id}:oidc-provider/${var.eks_oidc_provider}"
      ]
    }
    condition {
      test     = "StringEquals"
      variable = "${var.eks_oidc_provider}:sub"
      values = ["system:serviceaccount:default:${local.service_account_sa_name}"]
    }
    condition {
      test     = "StringEquals"
      variable = "${var.eks_oidc_provider}:aud"
      values = ["sts.amazonaws.com"]
    }
  }
}
