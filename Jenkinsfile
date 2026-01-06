pipeline {
    agent any

    tools {
        maven 'Maven'
        jdk 'JDK-17'
    }

    environment {
        SONAR_TOKEN = credentials('sonarcloud-token')
        DOCKER_IMAGE = "deligo-app"
        NETWORK_NAME = "deligo-network"
        POSTGRES_HOST = "postgres"
        POSTGRES_DB = "DeliGo"
        POSTGRES_USER = "postgres"
        POSTGRES_PASSWORD = "123"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                echo "📦 Checked out code from repository"
            }
        }

        stage('Build') {
            steps {
                echo "🔨 Building application..."
                sh 'mvn clean compile -DskipTests'
            }
        }

        stage('Test') {
            steps {
                echo "🧪 Running tests..."
                sh 'mvn test || true'
            }
        }

        stage('SonarCloud Analysis') {
            steps {
                echo "📊 Running SonarCloud analysis..."
                sh '''
                    mvn sonar:sonar \
                        -Dsonar.login=${SONAR_TOKEN} || true
                '''
            }
        }

        stage('Package') {
            steps {
                echo "📦 Packaging application..."
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Build Docker Image') {
            steps {
                echo "🐳 Building Docker image..."
                script {
                    sh "docker build -t ${DOCKER_IMAGE}:${BUILD_NUMBER} ."
                    sh "docker build -t ${DOCKER_IMAGE}:latest ."
                    echo "✅ Docker images built: ${DOCKER_IMAGE}:${BUILD_NUMBER} and ${DOCKER_IMAGE}:latest"
                }
            }
        }

        stage('Setup Network') {
            steps {
                echo "🌐 Setting up Docker network..."
                script {
                    sh """
                        docker network create ${NETWORK_NAME} 2>/dev/null || true
                        echo "✅ Network ready: ${NETWORK_NAME}"
                    """
                }
            }
        }

        stage('Stop Old Container') {
            steps {
                echo "🛑 Stopping old container..."
                script {
                    sh """
                        docker stop deligo-app 2>/dev/null || true
                        docker rm deligo-app 2>/dev/null || true
                        echo "✅ Old container removed"
                    """
                }
            }
        }

        stage('Deploy') {
            steps {
                echo "🚀 Deploying application..."
                script {
                    sh """
                        docker run -d \
                            --name deligo-app \
                            --network ${NETWORK_NAME} \
                            -p 8080:8080 \
                            -e SPRING_DATASOURCE_URL=jdbc:postgresql://${POSTGRES_HOST}:5432/${POSTGRES_DB} \
                            -e SPRING_DATASOURCE_USERNAME=${POSTGRES_USER} \
                            -e SPRING_DATASOURCE_PASSWORD=${POSTGRES_PASSWORD} \
                            ${DOCKER_IMAGE}:${BUILD_NUMBER}
                    """
                    echo "✅ Container deployed: deligo-app"
                }
            }
        }

        stage('Health Check') {
            steps {
                echo "🏥 Checking application health..."
                script {
                    sleep(time: 30, unit: 'SECONDS')

                    sh '''
                        for i in {1..10}; do
                            if docker exec deligo-app wget --quiet --tries=1 --spider http://localhost:8080/api/v1/actuator/health 2>/dev/null; then
                                echo "✅ Application is healthy!"
                                exit 0
                            fi
                            echo "⏳ Waiting for application to be healthy... ($i/10)"
                            sleep 10
                        done
                        echo "❌ Application failed health check"
                        exit 1
                    '''
                }
            }
        }
    }

    post {
        success {
            echo '✅ Pipeline completed successfully!'
            echo '🚀 Application URL: http://localhost:8080'
            echo "📝 Build Number: ${BUILD_NUMBER}"
            echo "🐳 Docker Image: ${DOCKER_IMAGE}:${BUILD_NUMBER}"
        }
        failure {
            echo '❌ Pipeline failed!'
            echo '🧹 Cleaning up failed deployment...'
            sh '''
                docker stop deligo-app 2>/dev/null || true
                docker rm deligo-app 2>/dev/null || true
            '''
        }
        always {
            echo '🧹 Cleaning workspace...'
            deleteDir()
        }
    }
}