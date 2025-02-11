def call(Map pipelineParams) {
    podTemplate(
            name: "build-${pipelineParams.libName}-${BUILD_ID}",
            agentContainer: 'maven', agentInjection: true, serviceAccount: 'jenkins-cicd-sa',
            containers: [
                    containerTemplate(
                            name: 'maven', image: 'maven:3.9-eclipse-temurin-21',
                            envVars: [secretEnvVar(key: 'AWS_CODE_ARTIFACT_AUTH_TOKEN', secretName: 'aws-code-artifact-token', secretKey: 'AWS_CODE_ARTIFACT_AUTH_TOKEN')]
                    )
            ],
            volumes: [
                    configMapVolume(configMapName: 'maven-settings', mountPath: '/root/.m2'),
                    persistentVolumeClaim(claimName: 'maven-local-repo', mountPath: '/root/.m2repo')
            ]) {
        node(POD_LABEL) {
            stage('Checkout') {
                echo 'Checking out source code...'
                checkout scm
            }

            stage('Build') {
                echo 'Building the project...'

                sh "mvn -f ${WORKSPACE}/${pipelineParams.libName}/pom.xml clean compile"
            }

            stage('Unit Test') {
                echo 'Running unit tests...'

                sh "mvn -f ${WORKSPACE}/${pipelineParams.libName}/pom.xml test"
            }

            stage('Integration Test') {
                echo 'Running integration tests...'

                sh "mvn -f ${WORKSPACE}/${pipelineParams.libName}/pom.xml verify"
            }

            stage('Package') {
                echo 'Packaging library...'

                sh "mvn -f ${WORKSPACE}/${pipelineParams.libName}/pom.xml package"
            }

            stage('Deploy') {
                echo 'Deploying library...'

                sh "cat /root/.m2/settings.xml"
                sh "mvn help:effective-settings"
                sh "mvn -f ${WORKSPACE}/${pipelineParams.libName}/pom.xml deploy"
            }
        }
    }
}
