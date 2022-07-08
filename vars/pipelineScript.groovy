def call(body) {

    def pipelineParams= [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = pipelineParams
    body()
    pipeline {
        environment {
            registryCredential = 'k8sregistry'
            JAVA_TOOL_OPTIONS = "-Duser.home=/var/maven"
            GIT_REPO_NAME = "${pipelineParams.appName != null ? pipelineParams.appName : env.GIT_URL.replaceFirst(/^.*\/([^\/]+?).git$/, '$1')}"
            IS_DEPLOY = "${pipelineParams.isDeploy != null ? pipelineParams.isDeploy : false}"
            GIT_REPO_NAME2 = env.GIT_URL.replaceFirst(/^.*\/([^\/]+?).git$/, '$1')
            IS_RELEASE = "${(env.GIT_BRANCH.contains('release') || env.GIT_BRANCH.contains('feature'))}"
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
                    sh "echo ${GIT_REPO_NAME}"
                    sh "echo ${GIT_REPO_NAME2}"
                    sh "echo ${GIT_BRANCH}"
                }
            }
            stage('test') {
               when {
                 anyOf {
                     branch 'master'; branch 'develop'
                     expression{env.IS_DEPLOY.toBoolean() == true}
                       }
                    }
                steps {
                    testEcho("""${env.REGISTRY_URL}""", '2nd' , """ABC XYZ ${pipelineParams.branch} ABC XYZ""", "fourth", '5th')
                }
            }
            stage('test B') {
                steps {
                    quoteTest('abc')
                }
            }
            stage('Get Repo Details') {
                steps {
                    script {
                    Map<String, String> details = getNexusRepositoryDetails(env.IS_RELEASE.toBoolean())
                    }
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
    sh ''' echo "\${abc}" ''' + abc
}

Map<String, String> getNexusRepositoryDetails(boolean isRelease) {

    Map<String, String> repositoryDetailsMap = new HashMap<String, String>()
    def mavenPom = readMavenPom file: 'pom.xml'
    
    echo(isRelease.toString())

    if (isRelease || !mavenPom.version.endsWith('SNAPSHOT')) {
        echo("if")
        repositoryDetailsMap.put("credentialsId", "jenkins-nuro-nexus")
       // repositoryDetailsMap.put("repositoryId", mavenPom.distributionManagement.snapshotRepository.id.toString())
       // repositoryDetailsMap.put("repositoryUrl", mavenPom.distributionManagement.snapshotRepository.url.toString())
    } else {
        echo("else")
        repositoryDetailsMap.put("credentialsId", "jenkins-nero-release-nexus")
      //  repositoryDetailsMap.put("repositoryId", mavenPom.distributionManagement.repository.id.toString())
      //  repositoryDetailsMap.put("repositoryUrl", mavenPom.distributionManagement.repository.ur.toString())
    }
    return repositoryDetailsMap;
}
