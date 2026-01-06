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
        POSTGRES_HOST = "deligo-postgres"
        POSTGRES_DB = "DeliGo"
        POSTGRES_USER = "postgres"
        POSTGRES_PASSWORD = "123"

        // JWT Configuration
        JWT_SECRET = credentials('jwt-secret')
        JWT_EXPIRATION = 86400000
        JWT_REFRESH_EXPIRATION = 604800000

        // OAuth2 - Google
        GOOGLE_CLIENT_ID = credentials('google-client-id')
        GOOGLE_CLIENT_SECRET = credentials('google-client-secret')

        // OAuth2 - Facebook
        FACEBOOK_CLIENT_ID = credentials('facebook-client-id')
        FACEBOOK_CLIENT_SECRET = credentials('facebook-client-secret')

        // OAuth2 - Auth0
        AUTH0_CLIENT_ID = credentials('auth0-client-id')
        AUTH0_CLIENT_SECRET = credentials('auth0-client-secret')
        AUTH0_ISSUER_URI = credentials('auth0-issuer-uri')

        // OAuth2 Redirect URI
        OAUTH2_REDIRECT_URI = "http://localhost:4200/oauth2/redirect"
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
                        docker network create ${NETWORK_NAME} 2>/dev/null || echo "Network already exists"
                        docker network connect ${NETWORK_NAME} ${POSTGRES_HOST} 2>/dev/null || echo "Postgres already connected"
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
                            -e JWT_SECRET=${JWT_SECRET} \
                            -e JWT_EXPIRATION=${JWT_EXPIRATION} \
                            -e JWT_REFRESH_EXPIRATION=${JWT_REFRESH_EXPIRATION} \
                            -e GOOGLE_CLIENT_ID=${GOOGLE_CLIENT_ID} \
                            -e GOOGLE_CLIENT_SECRET=${GOOGLE_CLIENT_SECRET} \
                            -e FACEBOOK_CLIENT_ID=${FACEBOOK_CLIENT_ID} \
                            -e FACEBOOK_CLIENT_SECRET=${FACEBOOK_CLIENT_SECRET} \
                            -e AUTH0_CLIENT_ID=${AUTH0_CLIENT_ID} \
                            -e AUTH0_CLIENT_SECRET=${AUTH0_CLIENT_SECRET} \
                            -e AUTH0_ISSUER_URI=${AUTH0_ISSUER_URI} \
                            -e OAUTH2_REDIRECT_URI=${OAUTH2_REDIRECT_URI} \
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
                    echo "⏳ Waiting 30 seconds for application to start..."
                    sleep(time: 30, unit: 'SECONDS')

                    sh '''
                        for i in 1 2 3 4 5 6 7 8 9 10; do
                            if docker exec deligo-app wget --quiet --tries=1 --spider http://localhost:8080/api/v1/actuator/health 2>/dev/null; then
                                echo "✅ Application is healthy!"
                                exit 0
                            fi
                            echo "⏳ Waiting for application to be healthy... (Attempt $i/10)"
                            sleep 10
                        done
                        echo "❌ Application failed health check after 10 attempts"
                        echo "📋 Checking application logs..."
                        docker logs deligo-app --tail 50
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