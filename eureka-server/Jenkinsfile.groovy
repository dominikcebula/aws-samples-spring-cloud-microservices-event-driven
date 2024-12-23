podTemplate(agentContainer: 'maven', agentInjection: true, containers: [
        containerTemplate(name: 'maven', image: 'maven:3.9-eclipse-temurin-21')
]) {
    node(POD_LABEL) {
        environment {
            AWS_REGION = 'eu-central-1'
            ECR_REPO = 'eureka-server'
            IMAGE_TAG = "${env.BUILD_ID}"
            DOCKER_REGISTRY = "<your_account_id>.dkr.ecr.${AWS_REGION}.amazonaws.com"
            KUBECONFIG_CREDENTIALS_ID = 'your-kubeconfig-credentials-id'
        }

        stage('Checkout') {
            steps {
                echo 'Checking out source code...'
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo 'Building the project...'
                sh 'mvn clean compile'
            }
        }

        stage('Unit Test') {
            steps {
                echo 'Running unit tests...'
                sh 'mvn test'
            }
        }

        stage('Integration Test') {
            steps {
                echo 'Running integration tests...'
                sh 'mvn verify'
            }
        }

        stage('Package') {
            steps {
                echo 'Packaging the application...'
                sh 'mvn package'
            }
        }

        stage('Containerize') {
            steps {
                script {
                    echo 'Building Docker image...'
                    sh "docker build -t ${DOCKER_REGISTRY}/${ECR_REPO}:${IMAGE_TAG} ."
                }
            }
        }

        stage('Deploy to AWS ECR') {
            steps {
                script {
                    echo 'Logging in to AWS ECR...'
                    sh "aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${DOCKER_REGISTRY}"

                    echo 'Pushing Docker image to ECR...'
                    sh "docker push ${DOCKER_REGISTRY}/${ECR_REPO}:${IMAGE_TAG}"
                }
            }
        }

        stage('Deploy to EKS') {
            steps {
                withCredentials([file(credentialsId: KUBECONFIG_CREDENTIALS_ID, variable: 'KUBECONFIG')]) {
                    script {
                        echo 'Deploying to EKS...'
                        //sh 'kubectl apply -f k8s/deployment.yaml'
                        //sh 'kubectl apply -f k8s/service.yaml'
                    }
                }
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
