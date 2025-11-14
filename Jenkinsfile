pipeline {
        agent any

        tools {
            maven 'maven-3.8.5'
            jdk 'jdk-17'
            sonarQubeScanner 'sonar-scanner'
        }

        environment {
            SONAR_SCANNER_HOME = tool 'SonarQubeScanner'
        }
        stages {
            stage('Checkout') {
                steps {
                    checkout scm
                }
            }

            stage('Build') {
                steps {
                    sh 'mvn clean compile'
                }
            }

            stage('Unit Tests') {
                steps {
                    sh 'mvn test -Dtest=!TestSuiteRunner,!LogisticsApplicationTests'

                }
                post {
                    always {
                        junit 'target/surefire-reports/*.xml'
                    }
                }
            }

        stage('SonarQube Analysis') {
                   steps {
                       withSonarQubeEnv('sonarqube') {
                           sh 'mvn sonar:sonar -Dsonar.projectKey=logistics -Dsonar.projectName="logistics"'
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