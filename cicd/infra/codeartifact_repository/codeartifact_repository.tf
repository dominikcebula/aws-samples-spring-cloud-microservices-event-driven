resource "aws_codeartifact_repository" "this" {
  repository = "aws-codeartifact-maven-snapshots"
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

    resources = ["arn:aws:codeartifact:${var.region}:${data.aws_caller_identity.current.account_id}:*"]
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

    resources = ["arn:aws:codeartifact:${var.region}:${data.aws_caller_identity.current.account_id}:*"]
  }
}

resource "aws_codeartifact_repository_permissions_policy" "this" {
  repository      = aws_codeartifact_repository.this.repository
  domain          = aws_codeartifact_domain.this.domain
  policy_document = data.aws_iam_policy_document.repository_policy.json
}
