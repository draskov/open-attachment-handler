pipeline {
agent {
        kubernetes {
            label 'Attachment Manager-' + BUILD_NUMBER
            defaultContainer "maven"
            yamlFile "pod.yaml"
        }
    }
    options {
        disableConcurrentBuilds()
    }
    
    stages {
        stage('Build') {
            steps {
                   script {
                       withCredentials([
                                file(credentialsId: '9d6421b7-5960-486d-a258-abc6aaab397a', variable: 'settingsFile')
                            ]) {
                            sh """
                                mvn deploy -s $settingsFile
                                """
                        }

                   }
                }
            }
        }
}
