resource "aws_codeartifact_repository" "this" {
  repository = "spring-cloud-microservices-event-driven"
  domain     = aws_codeartifact_domain.this.domain
}

data "aws_iam_policy_document" "repository_policy" {
  statement {
    effect = "Allow"

    principals {
      type = "AWS"
      identifiers = [data.aws_caller_identity.current.arn]
    }

    actions = [
      "codeartifact:AssociateExternalConnection",
      "codeartifact:CopyPackageVersions",
      "codeartifact:DeletePackageVersions",
      "codeartifact:DeletePackage",
      "codeartifact:DeleteRepository",
      "codeartifact:DeleteRepositoryPermissionsPolicy",
      "codeartifact:DescribePackageVersion",
      "codeartifact:DescribeRepository",
      "codeartifact:DisassociateExternalConnection",
      "codeartifact:DisposePackageVersions",
      "codeartifact:GetPackageVersionReadme",
      "codeartifact:GetRepositoryEndpoint",
      "codeartifact:ListPackageVersionAssets",
      "codeartifact:ListPackageVersionDependencies",
      "codeartifact:ListPackageVersions",
      "codeartifact:ListPackages",
      "codeartifact:PublishPackageVersion",
      "codeartifact:PutPackageMetadata",
      "codeartifact:PutRepositoryPermissionsPolicy",
      "codeartifact:ReadFromRepository",
      "codeartifact:UpdatePackageVersionsStatus",
      "codeartifact:UpdateRepository"
    ]

    resources = [aws_codeartifact_repository.this.arn]
  }

  statement {
    effect = "Allow"

    principals {
      type = "AWS"
      identifiers = [var.jenkins_cicd_role_arn]
    }

    actions = [
      "codeartifact:DescribePackageVersion",
      "codeartifact:DescribeRepository",
      "codeartifact:GetPackageVersionReadme",
      "codeartifact:GetRepositoryEndpoint",
      "codeartifact:ListPackageVersionAssets",
      "codeartifact:ListPackageVersionDependencies",
      "codeartifact:ListPackageVersions",
      "codeartifact:ListPackages",
      "codeartifact:PublishPackageVersion",
      "codeartifact:PutPackageMetadata",
      "codeartifact:ReadFromRepository"
    ]

    resources = [aws_codeartifact_repository.this.arn]
  }
}

resource "aws_codeartifact_repository_permissions_policy" "this" {
  repository      = aws_codeartifact_repository.this.repository
  domain          = aws_codeartifact_domain.this.domain
  policy_document = data.aws_iam_policy_document.repository_policy.json
}
