pipeline {
        agent any

        tools {
            maven 'maven-3.8.5'
            jdk 'jdk-17'
        }


        stages {
            stage('Checkout') {
                steps {
                    checkout scm
                }
            }

            stage('Build & test') {

                steps {

                     sh 'mvn clean verify'

                }
                post {
                    always {
                        junit 'target/surefire-reports/*.xml'
                    }
                }
            }

       stage('SonarQube Analysis') {
           steps {
               script {
                   withSonarQubeEnv('sonarqube') {
                       sh '''
                           mvn sonar:sonar \
                           -Dsonar.projectKey=logistics \
                           -Dsonar.projectName="logistics" \
                           -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml \
                           -Dsonar.host.url=http://sonarqube:9000
                       '''
                   }
               }
           }
       }


            stage('Package') {
                steps {
                    sh 'mvn package -DskipTests'
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        post {
            always {
                echo "Build ${currentBuild.result} - ${env.JOB_NAME} #${env.BUILD_NUMBER}"
            }
            success {
                echo 'Pipeline exécuté avec succès!'
            }
            failure {
                echo 'Pipeline a échoué!'
            }
        }
    }