def call(Map pipelineParams) {
    podTemplate(agentContainer: 'maven', agentInjection: true, serviceAccount: 'jenkins-cicd-sa', containers: [
            containerTemplate(name: 'maven', image: 'maven:3.9-eclipse-temurin-21'),
            containerTemplate(name: 'kaniko', image: "gcr.io/kaniko-project/executor:debug", command: '/busybox/cat', ttyEnabled: true),
            containerTemplate(name: 'awscli', image: 'amazon/aws-cli:2.22.26', command: 'cat', ttyEnabled: true),
            containerTemplate(name: 'kubectl', image: 'alpine/k8s:1.29.12', command: 'cat', ttyEnabled: true, runAsUser: '0')
    ], volumes: [
            persistentVolumeClaim(claimName: 'maven-repo', mountPath: '/root/.m2/repository'),
            configMapVolume(configMapName: 'kaniko-config', mountPath: '/kaniko/.docker'),
            emptyDirVolume(mountPath: '/.kube')
    ]) {
        node(POD_LABEL) {
//            stage('Checkout') {
//                echo 'Checking out source code...'
//                checkout scm
//            }
//
//            stage('Build') {
//                echo 'Building the project...'
//                sh 'mvn clean compile'
//            }
//
//            stage('Unit Test') {
//                echo 'Running unit tests...'
//                sh 'mvn test'
//            }
//
//            stage('Integration Test') {
//                echo 'Running integration tests...'
//                sh 'mvn verify'
//            }
//
//            stage('Package') {
//                echo 'Packaging the application...'
//                sh 'mvn package'
//            }
//
//            stage('Containerize') {
//                container(name: 'kaniko', shell: '/busybox/sh') {
//                    echo 'Building and uploading docker image using kaniko...'
//                    def ecrImageUrl = "${ECR_REPO_NAMESPACE_URL}/${pipelineParams.serviceName}:latest"
//                    sh "/kaniko/executor --dockerfile Dockerfile --context `pwd`/eureka-server --destination ${ecrImageUrl}"
//                }
//            }
//
//            stage('Configure Kubernetes Client') {
//                container(name: 'awscli') {
//                    sh "aws eks update-kubeconfig --name ${AWS_EKS_CLUSTER_NAME} --region ${AWS_REGION} --kubeconfig /.kube/config"
//                }
//            }

            stage('Deploy') {
                container(name: 'kubectl') {
                    sh """
                         #!/bin/bash

                         for file in ${pipelineParams.serviceName}/deployment/*.yaml
                         do
                             echo "Applying \${file}..."
                             envsubst < \${file} | KUBECONFIG=/.kube/config kubectl apply -f -
                             echo "Finished applying \$(basename \${file})...\n"
                         done
                        """
                }
            }
        }
    }
}
