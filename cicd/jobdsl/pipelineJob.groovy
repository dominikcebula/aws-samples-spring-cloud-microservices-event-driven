def call() {
    pipelineJob('eureka-server') {
        definition {
            cpsScm {
                scm {
                    git {
                        remote {
                            url('https://github.com/dominikcebula/aws-samples-spring-cloud-microservices-event-driven.git')
                            credentials('jenkins-github-token')
                        }
                        branch('*/main')
                    }
                }
                scriptPath('eureka-server/Jenkinsfile.groovy')
                lightweight()
            }
        }
    }
}
