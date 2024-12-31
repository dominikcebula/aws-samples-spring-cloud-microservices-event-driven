locals {
  jenkins_cicd_role_name = "jenkins-cicd-role"
  jenkins_cicd_sa_name = "jenkins-cicd-sa"
}

resource "aws_iam_role" "jenkins_cicd_role" {
  name = local.jenkins_cicd_role_name

  assume_role_policy = data.aws_iam_policy_document.eks_assume_role_policy.json
}

data "aws_iam_policy_document" "eks_assume_role_policy" {
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
        "arn:aws:iam::${data.aws_caller_identity.current.account_id}:oidc-provider/${var.eks_oidc_provider}"
      ]
    }
    condition {
      test     = "StringEquals"
      variable = "${var.eks_oidc_provider}:sub"
      values = ["system:serviceaccount:default:${local.jenkins_cicd_sa_name}"]
    }
    condition {
      test     = "StringEquals"
      variable = "${var.eks_oidc_provider}:aud"
      values = ["sts.amazonaws.com"]
    }
  }
}

data "aws_caller_identity" "current" {}

resource "aws_eks_access_entry" "eks_access" {
  cluster_name  = var.eks_cluster_name
  principal_arn = aws_iam_role.jenkins_cicd_role.arn
  type          = "STANDARD"
}

resource "aws_eks_access_policy_association" "AmazonEKSAdminPolicy" {
  cluster_name  = var.eks_cluster_name
  policy_arn    = "arn:aws:eks::aws:cluster-access-policy/AmazonEKSAdminPolicy"
  principal_arn = aws_eks_access_entry.eks_access.principal_arn

  access_scope {
    type = "cluster"
  }
}

resource "aws_eks_access_policy_association" "AmazonEKSClusterAdminPolicy" {
  cluster_name  = var.eks_cluster_name
  policy_arn    = "arn:aws:eks::aws:cluster-access-policy/AmazonEKSClusterAdminPolicy"
  principal_arn = aws_eks_access_entry.eks_access.principal_arn

  access_scope {
    type = "cluster"
  }
}

resource "aws_iam_policy_attachment" "AmazonEC2ContainerRegistryFullAccess" {
  name       = "AmazonEC2ContainerRegistryFullAccess"
  roles = [aws_iam_role.jenkins_cicd_role.name]
  policy_arn = data.aws_iam_policy.AmazonEC2ContainerRegistryFullAccess.arn
}

data "aws_iam_policy" "AmazonEC2ContainerRegistryFullAccess" {
  name = "AmazonEC2ContainerRegistryFullAccess"
}

resource "aws_iam_policy_attachment" "AmazonEKSClusterPolicy" {
  name       = "AmazonEKSClusterPolicy"
  roles = [aws_iam_role.jenkins_cicd_role.name]
  policy_arn = data.aws_iam_policy.AmazonEKSClusterPolicy.arn
}

data "aws_iam_policy" "AmazonEKSClusterPolicy" {
  name = "AmazonEKSClusterPolicy"
}

resource "aws_iam_policy_attachment" "AmazonEKSWorkerNodePolicy" {
  name       = "AmazonEKSWorkerNodePolicy"
  roles = [aws_iam_role.jenkins_cicd_role.name]
  policy_arn = data.aws_iam_policy.AmazonEKSWorkerNodePolicy.arn
}

data "aws_iam_policy" "AmazonEKSWorkerNodePolicy" {
  name = "AmazonEKSWorkerNodePolicy"
}
