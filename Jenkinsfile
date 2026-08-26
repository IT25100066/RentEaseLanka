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

        // ── Build both custom service images using the bound .env file ──
        stage('Build Images') {
            steps {
                withCredentials([file(credentialsId: 'renteaselanka_credentials', variable: 'ENV_FILE_PATH')]) {
                    bat 'docker compose --env-file "%ENV_FILE_PATH%" build'
                }
            }
        }

        // ── Tag images with the Jenkins build number ────────────────────
        stage('Tag Images') {
            steps {
                bat """
                    docker tag ${DOCKERHUB_USERNAME}/${APP_NAME}-backend:latest ${DOCKERHUB_USERNAME}/${APP_NAME}-backend:${IMAGE_TAG}
                    docker tag ${DOCKERHUB_USERNAME}/${APP_NAME}-frontend:latest ${DOCKERHUB_USERNAME}/${APP_NAME}-frontend:${IMAGE_TAG}
                """
            }
        }

        // ── Push tagged and latest images to Docker Hub ─────────────────
        stage('Push to Docker Hub') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-credentials',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    bat '''
                echo %DOCKER_PASS%| docker login -u %DOCKER_USER% --password-stdin
                docker push %DOCKERHUB_USERNAME%/%APP_NAME%-backend:%IMAGE_TAG%
                docker push %DOCKERHUB_USERNAME%/%APP_NAME%-backend:latest
                docker push %DOCKERHUB_USERNAME%/%APP_NAME%-frontend:%IMAGE_TAG%
                docker push %DOCKERHUB_USERNAME%/%APP_NAME%-frontend:latest
                docker logout
            '''
                }
            }
        }

        // ── Cleanup build-tagged images to save disk space ──────────────
        stage('Cleanup') {
            steps {
                bat """
                    docker compose down --rmi local || exit 0
                    docker rmi ${DOCKERHUB_USERNAME}/${APP_NAME}-backend:${IMAGE_TAG} || exit 0
                    docker rmi ${DOCKERHUB_USERNAME}/${APP_NAME}-frontend:${IMAGE_TAG} || exit 0
                """
            }
        }
    }

    post {
        success {
            echo """
            =======================================================
            ✅ All images built, tagged, and pushed successfully!
            Backend:  ${DOCKERHUB_USERNAME}/${APP_NAME}-backend:${IMAGE_TAG}
            Frontend: ${DOCKERHUB_USERNAME}/${APP_NAME}-frontend:${IMAGE_TAG}
            =======================================================
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