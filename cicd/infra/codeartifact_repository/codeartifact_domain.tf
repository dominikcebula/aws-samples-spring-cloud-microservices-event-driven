resource "aws_codeartifact_domain" "this" {
  domain = "spring-cloud-microservices-event-driven"
}

data "aws_iam_policy_document" "domain_policy" {
  statement {
    effect = "Allow"

    principals {
      type = "AWS"
      identifiers = [data.aws_caller_identity.current.arn]
    }

    actions = [
      "codeartifact:CreateRepository",
      "codeartifact:DeleteDomain",
      "codeartifact:DeleteDomainPermissionsPolicy",
      "codeartifact:DescribeDomain",
      "codeartifact:GetAuthorizationToken",
      "codeartifact:GetDomainPermissionsPolicy",
      "codeartifact:ListRepositoriesInDomain",
      "codeartifact:PutDomainPermissionsPolicy",
      "sts:GetServiceBearerToken"
    ]

    resources = [aws_codeartifact_domain.this.arn]
  }

  statement {
    effect = "Allow"

    principals {
      type = "AWS"
      identifiers = [var.jenkins_cicd_role_arn]
    }

    actions = [
      "codeartifact:CreateRepository",
      "codeartifact:DescribeDomain",
      "codeartifact:GetAuthorizationToken",
      "codeartifact:GetDomainPermissionsPolicy",
      "codeartifact:ListRepositoriesInDomain",
      "sts:GetServiceBearerToken"
    ]

    resources = [aws_codeartifact_domain.this.arn]
  }
}

resource "aws_codeartifact_domain_permissions_policy" "this" {
  domain          = aws_codeartifact_domain.this.domain
  policy_document = data.aws_iam_policy_document.domain_policy.json
}
