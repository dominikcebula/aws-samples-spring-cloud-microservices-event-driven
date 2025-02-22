def call(Map pipelineParams) {
    podTemplate(name: "build-${pipelineParams.serviceName}-${BUILD_ID}", agentContainer: 'maven', agentInjection: true, serviceAccount: 'jenkins-cicd-sa', containers: [
            containerTemplate(name: 'maven', image: 'maven:3.9-eclipse-temurin-21'),
            containerTemplate(name: 'kaniko', image: "gcr.io/kaniko-project/executor:debug", command: '/busybox/cat', ttyEnabled: true),
            containerTemplate(name: 'awscli', image: 'amazon/aws-cli:2.22.26', command: 'cat', ttyEnabled: true),
            containerTemplate(name: 'kubectl', image: 'alpine/k8s:1.29.12', command: 'cat', ttyEnabled: true, runAsUser: '0'),
            containerTemplate(name: 'docker-daemon', image: 'docker:27.5.1-dind', privileged: 'true'),
    ], volumes: [
            persistentVolumeClaim(claimName: 'maven-local-repo', mountPath: '/root/.m2/repository'),
            configMapVolume(configMapName: 'kaniko-config', mountPath: '/kaniko/.docker'),
            emptyDirVolume(mountPath: '/.kube'),
            emptyDirVolume(name: 'docker-socket', mountPath: '/var/run')
    ]) {
        node(POD_LABEL) {
            stage('Checkout') {
                echo 'Checking out source code...'
                checkout scm
            }

            stage('Build') {
                echo 'Building the project...'
                sh "mvn -f ${WORKSPACE}/${pipelineParams.serviceName}/pom.xml clean compile"
            }

            stage('Unit Test') {
                echo 'Running unit tests...'
                sh "mvn -f ${WORKSPACE}/${pipelineParams.serviceName}/pom.xml test"
            }

            stage('Integration Test') {
                echo 'Running integration tests...'
                sh "mvn -f ${WORKSPACE}/${pipelineParams.serviceName}/pom.xml verify"
            }

            stage('Package') {
                echo 'Packaging the application...'
                sh "mvn -f ${WORKSPACE}/${pipelineParams.serviceName}/pom.xml package"
            }

            stage('Containerize') {
                container(name: 'kaniko', shell: '/busybox/sh') {
                    echo 'Building and uploading docker image using kaniko...'
                    def ecrImageUrl = "${ECR_REPO_NAMESPACE_URL}/${pipelineParams.serviceName}:latest"
                    sh "/kaniko/executor --dockerfile cicd/docker/Dockerfile --context ${WORKSPACE}/${pipelineParams.serviceName} --destination ${ecrImageUrl}"
                }
            }

            stage('Configure Kubernetes Client') {
                container(name: 'awscli') {
                    sh "aws eks update-kubeconfig --name ${AWS_EKS_CLUSTER_NAME} --region ${AWS_REGION} --kubeconfig /.kube/config"
                }
            }

            stage('Deploy') {
                container(name: 'kubectl') {
                    sh """
                       #!/bin/bash

                       for file in ${pipelineParams.serviceName}/deployment/*.yaml
                       do
                           echo "Applying \${file}..."
                           envsubst < \${file} | KUBECONFIG=/.kube/config kubectl apply -f -
                           echo "Finished applying \$(basename \${file}).\n"
                       done
                       """
                }
            }
        }
    }
}
