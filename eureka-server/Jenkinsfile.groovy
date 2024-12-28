podTemplate(agentContainer: 'maven', agentInjection: true, containers: [
        containerTemplate(name: 'maven', image: 'maven:3.9-eclipse-temurin-21'),
        containerTemplate(name: 'kaniko', image: "gcr.io/kaniko-project/executor:debug", command: '/busybox/cat', ttyEnabled: true),
        containerTemplate(name: 'awscli', image: 'amazon/aws-cli:2.22.26', command: 'cat', ttyEnabled: true),
        containerTemplate(name: 'kubectl', image: 'bitnami/kubectl:1.29.11', shell: '/bin/bash', command: 'sleep', args: "infinity", ttyEnabled: true)
], volumes: [
        persistentVolumeClaim(claimName: 'maven-repo', mountPath: '/root/.m2/repository'),
        configMapVolume(configMapName: 'kaniko-config', mountPath: '/kaniko/.docker'),
        emptyDirVolume(mountPath: '/.kube')
]) {
    node(POD_LABEL) {
        stage('Checkout') {
            echo 'Checking out source code...'
            checkout scm
        }

        stage('Build') {
            echo 'Building the project...'
            sh 'mvn clean compile'
        }

        stage('Unit Test') {
            echo 'Running unit tests...'
            sh 'mvn test'
        }

        stage('Integration Test') {
            echo 'Running integration tests...'
            sh 'mvn verify'
        }

        stage('Package') {
            echo 'Packaging the application...'
            sh 'mvn package'
        }

        stage('Containerize') {
            container(name: 'kaniko', shell: '/busybox/sh') {
                echo 'Building and uploading docker image using kaniko...'
                def ecrImageUrl = "${ECR_REPO_NAMESPACE_URL}/eureka-server:latest"
                sh "/kaniko/executor --dockerfile Dockerfile --context `pwd`/eureka-server --destination ${ecrImageUrl}"
            }
        }

        stage('Configure Kubernetes Client') {
            container(name: 'awscli') {
                sh "aws eks update-kubeconfig --name ${AWS_EKS_CLUSTER_NAME} --region ${AWS_REGION} --kubeconfig /.kube/config"
                sh "chmod 0640 /.kube/config"
            }
        }

        stage('Deploy') {
            container(name: 'kubectl', shell: '/bin/bash') {
                sh "echo ok"
                sh "KUBECONFIG=/.kube/config kubectl -v=8 apply -f `pwd`/eureka-server/deployment/*.yaml"
            }
        }
    }
}
