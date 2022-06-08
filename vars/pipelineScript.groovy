def call(body) {

    def pipelineParams= [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = pipelineParams
    body()
    pipeline {
        environment {
            registryCredential = 'k8sregistry'
            JAVA_TOOL_OPTIONS = "-Duser.home=/var/maven"
            IS_DEPLOY = "${pipelineParams.isDeploy != null ? pipelineParams.isDeploy : false}"
        }
        agent {
            docker {
                image 'maven:3.8.2-adoptopenjdk-8'
                args "-v /tmp/maven2:/var/maven/.m2 -e MAVEN_CONFIG=/var/maven/.m2"
            }
        }
        options{
            timeout(time: 30, unit: "MINUTES")
        }
        stages {
            stage('Build') {
                when {
                    anyOf {
                        branch 'master' ; branch 'develop'
                        expresssion {env.IS_DEPLOY.toBoolean() == true}
                    }
                }
                steps {
                    sh 'mvn -B -DskipTests clean package'
                }
            }
        }
    }
}
