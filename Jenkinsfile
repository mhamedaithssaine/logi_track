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

        stage('Verify JaCoCo Report') {
            steps {
                script {
                    sh '''
                        echo "=== Vérification du rapport JaCoCo ==="
                        ls -la target/site/jacoco/ || echo "Dossier jacoco n'existe pas!"
                        if [ -f target/site/jacoco/jacoco.xml ]; then
                            echo "✅ jacoco.xml trouvé"
                            echo "Taille du fichier:"
                            du -h target/site/jacoco/jacoco.xml
                            echo "Premières lignes:"
                            head -30 target/site/jacoco/jacoco.xml
                        else
                            echo "❌ jacoco.xml NOT FOUND!"
                            exit 1
                        fi
                    '''
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
                            -Dsonar.java.coveragePlugin=jacoco \
                            -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml \
                            -Dsonar.junit.reportPaths=target/surefire-reports \
                            -Dsonar.host.url=http://sonarqube:9000 \
                            -Dsonar.sources=src/main/java \
                            -Dsonar.tests=src/test/java \
                            -Dsonar.java.binaries=target/classes \
                            -Dsonar.java.test.binaries=target/test-classes
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