pipeline {
    agent any

    environment {
        DOCKERHUB_USERNAME = 'it25100066'
        APP_NAME           = 'renteaselanka'
        IMAGE_TAG          = "${BUILD_NUMBER}"
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
                echo "Branch: ${env.BRANCH_NAME} | Build: #${BUILD_NUMBER}"
            }
        }

        // ── Build all custom service images ──────────────────
        stage('Build Images') {
            steps {
                echo "Building all services..."
                bat "docker-compose build"
            }
        }

        // ── Tag each image with build number ─────────────────
        stage('Tag Images') {
            steps {
                bat "docker tag ${DOCKERHUB_USERNAME}/${APP_NAME}-backend:latest ${DOCKERHUB_USERNAME}/${APP_NAME}-backend:${IMAGE_TAG}"
            }
        }

        // ── Push to Docker Hub ────────────────────────────────
        stage('Push to Docker Hub') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-credentials',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    // Pipe the token directly into stdin
                    bat "echo %DOCKER_PASS% | docker login -u %DOCKER_USER% --password-stdin" 
                    bat "docker login -u %DOCKER_USER% -p %DOCKER_PASS%"

                    // Push backend
                    bat "docker push ${DOCKERHUB_USERNAME}/${APP_NAME}-backend:${IMAGE_TAG}"
                    bat "docker push ${DOCKERHUB_USERNAME}/${APP_NAME}-backend:latest"

                    // // Push frontend
                    // bat "docker push ${DOCKERHUB_USERNAME}/${APP_NAME}-frontend:${IMAGE_TAG}"
                    // bat "docker push ${DOCKERHUB_USERNAME}/${APP_NAME}-frontend:latest"

                  
                    bat "docker logout"
                }
            }
        }

        // ── Cleanup local images ──────────────────────────────
        stage('Cleanup') {
            steps {
                bat "docker-compose down --rmi local || exit 0"
                bat "docker rmi ${DOCKERHUB_USERNAME}/${APP_NAME}-backend:${IMAGE_TAG} || exit 0"
                // bat "docker rmi ${DOCKERHUB_USERNAME}/${APP_NAME}-frontend:${IMAGE_TAG} || exit 0"
               
            }
        }
    }

    post {
        success {
            echo """
            ✅ All images pushed successfully
            Backend:  ${DOCKERHUB_USERNAME}/${APP_NAME}-backend:latest
            // Frontend: ${DOCKERHUB_USERNAME}/${APP_NAME}-frontend:latest
          
            """
        }
        failure {
            echo "❌ Pipeline failed on branch ${env.BRANCH_NAME}"
        }
        always {
            cleanWs()
        }
    }
}