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
                    testEcho("""${env.REGISTRY_URL}""", '2nd' , """ABC XYZ ${pipelineParams.branch} ABC XYZ""", "fourth", '5th')
                }
            }
            stage('test B') {
                steps {
                    quoteTest('abc')
                }
            }
        }
    }
}

def testEcho(String echoString){
    sh """ ${echoString} """
}

def testEcho(String registryUrl,String second, String tag, String options, String context){
    sh """ echo ${options} -t ${registryUrl}/${tag} ${context}  """
}

def quoteTest(abc){
    sh ''' echo ${abc} \${abc} '''
}