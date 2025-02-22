apiVersion: v1
kind: Secret
metadata:
  name: jenkins-github-token
stringData:
  username: ${JENKINS_GITHUB_TOKEN_USERNAME}
  password:  ${JENKINS_GITHUB_TOKEN_PASSWORD}
---
apiVersion: v1
kind: Secret
metadata:
  name: aws-code-artifact-token
stringData:
  AWS_CODE_ARTIFACT_AUTH_TOKEN: ${AWS_CODE_ARTIFACT_AUTH_TOKEN}
---
apiVersion: v1
kind: ConfigMap
metadata:
  name: maven-settings
data:
  settings.xml: |
    <settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
        <localRepository>/root/.m2repo</localRepository>

        <servers>
            <server>
                <id>aws-codeartifact-maven-snapshots</id>
                <username>aws</username>
                <password>${env.AWS_CODE_ARTIFACT_AUTH_TOKEN}</password>
            </server>
        </servers>
    </settings>
---
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: maven-local-repo
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 10Gi
---
apiVersion: v1
kind: ConfigMap
metadata:
  name: kaniko-config
data:
  config.json: |
    {
      "credsStore": "ecr-login"
    }
---
apiVersion: v1
kind: ConfigMap
metadata:
  name: jenkins-operator-user-configuration
data:
  01-global-node-properties.yaml: |
    jenkins:
      globalNodeProperties:
      - envVars:
          env:
          - key: AWS_ACCOUNT_ID
            value: ${AWS_ACCOUNT_ID}
          - key: AWS_REGION
            value: ${AWS_REGION}
          - key: ECR_REPO_HOSTNAME
            value: ${ECR_REPO_HOSTNAME}
          - key: ECR_REPO_NAMESPACE
            value: ${ECR_REPO_NAMESPACE}
          - key: ECR_REPO_NAMESPACE_URL
            value: ${ECR_REPO_NAMESPACE_URL}
          - key: AWS_EKS_CLUSTER_NAME
            value: ${AWS_EKS_CLUSTER_NAME}
          - key: AWS_CODEARTIFACT_MAVEN_SNAPSHOTS_URL
            value: ${AWS_CODEARTIFACT_MAVEN_SNAPSHOTS_URL}
  02-global-trusted-pipeline-libraries.yaml: |
    unclassified:
      globalLibraries:
        libraries:
          - name: "pipeline"
            defaultVersion: "main"
            retriever:
              modernSCM:
                libraryPath: "cicd/pipeline"
                scm:
                  git:
                    remote: https://github.com/dominikcebula/aws-samples-spring-cloud-microservices-event-driven.git
                    credentialsId: jenkins-github-token
---
apiVersion: v1
kind: ServiceAccount
metadata:
    name: jenkins-cicd-sa
    annotations:
        eks.amazonaws.com/role-arn: arn:aws:iam::${AWS_ACCOUNT_ID}:role/jenkins-cicd-role
---
apiVersion: jenkins.io/v1alpha2
kind: Jenkins
metadata:
  name: master
  namespace: default
spec:
  configurationAsCode:
    configurations:
    - name: jenkins-operator-user-configuration
    secret:
        name: ""
  groovyScripts:
    configurations:
    - name: jenkins-operator-user-configuration
    secret:
        name: ""
  jenkinsAPISettings:
    authorizationStrategy: createUser
  master:
    disableCSRFProtection: false
    containers:
      - name: jenkins-master
        image: jenkins/jenkins:lts
        imagePullPolicy: Always
        livenessProbe:
          failureThreshold: 12
          httpGet:
            path: /login
            port: http
            scheme: HTTP
          initialDelaySeconds: 100
          periodSeconds: 10
          successThreshold: 1
          timeoutSeconds: 5
        readinessProbe:
          failureThreshold: 10
          httpGet:
            path: /login
            port: http
            scheme: HTTP
          initialDelaySeconds: 80
          periodSeconds: 10
          successThreshold: 1
          timeoutSeconds: 1
        resources:
          requests:
            cpu: 250m
            memory: 256Mi
          limits:
            cpu: 1500m
            memory: 1Gi
    basePlugins:
      - name: kubernetes
        version: "4302.va_756e4b_67715"
      - name: workflow-job
        version: "1498.v33a_0c6f3a_4b_4"
      - name: workflow-aggregator
        version: "600.vb_57cdd26fdd7"
      - name: git
        version: "5.7.0"
      - name: job-dsl
        version: "1.90"
      - name: configuration-as-code
        version: "1932.v75cb_b_f1b_698d"
      - name: kubernetes-credentials-provider
        version: "1.262.v2670ef7ea_0c5"
      - name: maven-plugin
        version: "3.24"
      - name: build-pipeline-plugin
        version: "2.0.2"
      - name: blueocean
        version: "1.27.16"
  service:
    type: LoadBalancer
    port: 80
    loadBalancerSourceRanges: ["${MY_PUBLIC_IP}/32"]
  seedJobs:
    - id: jenkins-operator
      credentialType: usernamePassword
      credentialID: jenkins-github-token
      targets: "**/pipeline.jenkins"
      description: "aws-samples-spring-cloud-microservices-event-driven"
      repositoryBranch: main
      repositoryUrl: https://github.com/dominikcebula/aws-samples-spring-cloud-microservices-event-driven.git
