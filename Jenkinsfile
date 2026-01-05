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
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean compile -DskipTests'
            }
        }

        stage('SonarCloud Analysis') {
            steps {
                sh '''
                    mvn sonar:sonar \
                        -Dsonar.login=${SONAR_TOKEN} || true
                '''
            }
        }

        stage('Package') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Build Docker Image') {
            steps {
                sh 'docker build -t deligo-app:${BUILD_NUMBER} .'
                sh 'docker build -t deligo-app:latest .'
            }
        }
    }

    post {
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed!'
        }
        always {
            cleanWs()
        }
    }
}
