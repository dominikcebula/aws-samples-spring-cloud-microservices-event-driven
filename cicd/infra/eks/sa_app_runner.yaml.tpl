apiVersion: v1
kind: ServiceAccount
metadata:
  name: app-runner-sa
  annotations:
    eks.amazonaws.com/role-arn: arn:aws:iam::${AWS_ACCOUNT_ID}:role/app-runner
