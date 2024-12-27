podTemplate(agentContainer: 'maven', agentInjection: true, containers: [
        containerTemplate(name: 'maven', image: 'maven:3.9-eclipse-temurin-21'),
        containerTemplate(name: 'kaniko', image: "gcr.io/kaniko-project/executor:debug", command: '/busybox/cat', ttyEnabled: true)
], volumes: [genericEphemeralVolume(accessModes: 'ReadWriteOnce', mountPath: '/root/.m2/repository', requestsSize: '1Gi')]) {
    node(POD_LABEL) {
        environment {
            AWS_REGION = 'eu-central-1'
            ECR_REPO = 'eureka-server'
            IMAGE_TAG = "${env.BUILD_ID}"
            DOCKER_REGISTRY = "${AWS_ACCOUND_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
            KUBECONFIG_CREDENTIALS_ID = 'your-kubeconfig-credentials-id'
        }

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
                echo 'Building Docker image...'

                def kanikoConfigDirectory = new File('/kaniko/.docker')
                if (!kanikoConfigDirectory.exists())
                    kanikoConfigDirectory.mkdirs()

                def kanikoConfigContent = '''
                    {
                        "credsStore": "ecr-login"
                    }
                    '''
                def kanikoConfigFile = new File("/kaniko/.docker/config.json")
                kanikoConfigFile.write(kanikoConfigContent)
                kanikoConfigFile.close()

                sh "/kaniko/executor --dockerfile Dockerfile --context `pwd`/eureka-server --destination aws-samples-spring-cloud-microservices-event-driven/eureka-server:latest"
            }
        }
    }

    post {
        success {
            echo 'Pipeline executed successfully!'
        }
        failure {
            echo 'Pipeline failed!'
        }
        always {
            echo 'Cleaning up...'
            cleanWs()
        }
    }
}
