def call(body) {

    def pipelineParams= [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = pipelineParams
    body()
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
                  //  sh 'mvn -B -DskipTests clean package'
                    sh 'echo Build Step'
                }
            }
            stage('test') {
                steps {
                    testEcho("""${env.REGISTRY_URL}""",  """ABC XYZ ${pipelineParams.branch} ABC XYZ""", "fourth", '5th')
                }
            }
        }
    }
}

def testEcho(String echoString){
    sh """ ${echoString} """
}

def testEcho(String branch, String registryUrl, String test, String fourth, String fifth){
    sh """ echo ${branch} ${registryUrl} """
    sh """ echo ${registryUrl} """
    sh """ echo ${test} """
    sh """ echo ${fourth} """
    sh """ echo ${fifth} """
    sh """ echo ${fourth} -t ${registryUrl}/${test} ${fifth}  """
}