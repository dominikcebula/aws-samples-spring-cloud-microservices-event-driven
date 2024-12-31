apiVersion: v1
kind: ConfigMap
metadata:
  name: db-info-cm
data:
  RDS_HOSTNAME: ${RDS_HOSTNAME}
  RDS_PORT: ${RDS_PORT}
  RDS_DB_NAME: ${RDS_DB_NAME}
  RDS_USERNAME: ${RDS_USERNAME}
