podTemplate(agentContainer: 'maven', agentInjection: true, containers: [
        containerTemplate(name: 'maven', image: 'maven:3.9-eclipse-temurin-21'),
        containerTemplate(name: 'kaniko', image: "gcr.io/kaniko-project/executor:debug", command: '/busybox/cat', ttyEnabled: true)
], volumes: [genericEphemeralVolume(accessModes: 'ReadWriteOnce', mountPath: '/root/.m2/repository', requestsSize: '10Gi', storageClassName: 'gp2')]) {
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
                echo 'Preparing kaniko configuration...'
                sh 'echo "{ \\"credsStore\\": \\"ecr-login\\" }" > /kaniko/.docker/config.json'

                echo 'Building and uploading docker image using kaniko...'
                def ecrImageUrl = "${ECR_REPO_NAMESPACE_URL}/eureka-server:latest"
                sh "/kaniko/executor --dockerfile Dockerfile --context `pwd`/eureka-server --destination ${ecrImageUrl}"
            }
        }
    }
}
