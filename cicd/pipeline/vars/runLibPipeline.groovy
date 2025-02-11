def call(Map pipelineParams) {
    podTemplate(
            name: "build-${pipelineParams.libName}-${BUILD_ID}",
            agentContainer: 'maven', agentInjection: true, serviceAccount: 'jenkins-cicd-sa',
            containers: [
                    containerTemplate(name: 'maven', image: 'maven:3.9-eclipse-temurin-21',
                            envVars: [secretEnvVar(key: 'AWS_CODE_ARTIFACT_AUTH_TOKEN', secretName: 'aws-code-artifact-token', secretKey: 'AWS_CODE_ARTIFACT_AUTH_TOKEN')])
            ],
            volumes: [
                    persistentVolumeClaim(claimName: 'maven-repo', mountPath: '/root/.m2/repository')
            ]) {
        node(POD_LABEL) {
            stage('Checkout') {
                echo 'Checking out source code...'
                checkout scm
            }

            stage('Build') {
                echo 'Building the project...'

                configFileProvider([configFile(fileId: 'maven-settings', variable: 'MAVEN_SETTINGS')]) {
                    sh "env"
                    sh "mvn -s $MAVEN_SETTINGS -f ${WORKSPACE}/${pipelineParams.libName}/pom.xml clean compile"
                }
            }

            stage('Unit Test') {
                echo 'Running unit tests...'

                configFileProvider([configFile(fileId: 'maven-settings', variable: 'MAVEN_SETTINGS')]) {
                    sh "mvn -s $MAVEN_SETTINGS -f ${WORKSPACE}/${pipelineParams.libName}/pom.xml test"
                }
            }

            stage('Integration Test') {
                echo 'Running integration tests...'

                configFileProvider([configFile(fileId: 'maven-settings', variable: 'MAVEN_SETTINGS')]) {
                    sh "mvn -s $MAVEN_SETTINGS -f ${WORKSPACE}/${pipelineParams.libName}/pom.xml verify"
                }
            }

            stage('Package') {
                echo 'Packaging library...'

                configFileProvider([configFile(fileId: 'maven-settings', variable: 'MAVEN_SETTINGS')]) {
                    sh "mvn -s $MAVEN_SETTINGS -f ${WORKSPACE}/${pipelineParams.libName}/pom.xml package"
                }
            }

            stage('Deploy') {
                echo 'Deploying library...'

                configFileProvider([configFile(fileId: 'maven-settings', variable: 'MAVEN_SETTINGS')]) {
                    sh "mvn -s $MAVEN_SETTINGS -f ${WORKSPACE}/${pipelineParams.libName}/pom.xml deploy"
                }
            }
        }
    }
}
