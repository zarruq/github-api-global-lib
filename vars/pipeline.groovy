def call(int buildNumber){
    pipeline {
        environment {
            registryCredential = 'k8sregistry'
            JAVA_TOOL_OPTIONS = "-Duser.home=/var/maven"
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
                steps {
                    sh 'mvn -B -DskipTests clean package'
                }
            }
        }
    }
}