pipeline {
    agent any

    tools {
        maven 'Maven'
        jdk 'JDK-17'
    }

    environment {
        SONAR_TOKEN = credentials('sonarcloud-token')
    }

    stages {
        stage('Checkout') {
            steps {
                echo '📥 Getting code from GitHub...'
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo '🔨 Building application...'
                sh 'mvn clean compile'
            }
        }

        stage('Test & Coverage') {
            steps {
                echo '🧪 Running tests with JaCoCo coverage...'
                sh 'mvn test'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('SonarCloud Analysis') {
            steps {
                echo '📊 Analyzing code with SonarCloud...'
                sh '''
                    mvn sonar:sonar \
                        -Dsonar.login=${SONAR_TOKEN}
                '''
            }
        }

        stage('Package') {
            steps {
                echo '📦 Creating JAR file...'
                sh 'mvn package -DskipTests'
            }
        }

        stage('Build Docker Image') {
            steps {
                echo '🐳 Building Docker image...'
                sh 'docker build -t deligo-app:${BUILD_NUMBER} .'
                sh 'docker build -t deligo-app:latest .'
            }
        }
    }

    post {
        success {
            echo '✅ Pipeline completed successfully!'
            echo 'View SonarCloud report: https://sonarcloud.io/dashboard?id=theshamkhi_DeliGo'
        }
        failure {
            echo '❌ Pipeline failed!'
        }
        always {
            echo '🧹 Cleaning workspace...'
            cleanWs()
        }
    }
}