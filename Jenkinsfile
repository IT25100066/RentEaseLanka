pipeline {
    agent any

    environment {
        DOCKERHUB_USERNAME = 'it25100066'
        APP_NAME           = 'renteaselanka'
        IMAGE_TAG          = "${BUILD_NUMBER}"
        DEPLOY_BRANCH      = 'Ferature_Fleet_management'
        REMOTE_DIR         = '/opt/renteaselanka'
    }

    stages {

        // ── Checkout ────────────────────────────────────────────────────
        stage('Checkout') {
            steps {
                checkout scm
                echo "Build: #${BUILD_NUMBER} | Branch: ${env.BRANCH_NAME ?: 'unknown'}"
                echo "DEBUG branch vars: BRANCH_NAME=[${env.BRANCH_NAME}] GIT_BRANCH=[${env.GIT_BRANCH}] CHANGE_BRANCH=[${env.CHANGE_BRANCH}]"
                echo "DEBUG DEPLOY_BRANCH=[${env.DEPLOY_BRANCH}]"
            }
        }

        // ── Build Images ─────────────────────────────────────────────────
        stage('Build Images') {
            steps {
                withCredentials([file(credentialsId: 'renteaselanka_credentials', variable: 'ENV_FILE_PATH')]) {
                    bat 'docker compose --env-file "%ENV_FILE_PATH%" build'
                }
            }
        }

        // ── Tag Images ───────────────────────────────────────────────────
        stage('Tag Images') {
            steps {
                bat """
                    docker tag ${DOCKERHUB_USERNAME}/${APP_NAME}-backend:latest ${DOCKERHUB_USERNAME}/${APP_NAME}-backend:${IMAGE_TAG}
                    docker tag ${DOCKERHUB_USERNAME}/${APP_NAME}-frontend:latest ${DOCKERHUB_USERNAME}/${APP_NAME}-frontend:${IMAGE_TAG}
                """
            }
        }

        // ── Push to Docker Hub ───────────────────────────────────────────
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

        // ── Deploy to DigitalOcean (deploy branch only) ──────────────────
        stage('Deploy to DigitalOcean') {
            
            steps {
                withCredentials([
                    sshUserPrivateKey(
                        credentialsId    : 'do-ssh-key',
                        keyFileVariable  : 'SSH_KEY_FILE',
                        usernameVariable : 'SSH_USER'
                    ),
                    string(credentialsId: 'do-droplet-host',  variable: 'DO_HOST'),
                    string(credentialsId: 'do-domain',        variable: 'DO_DOMAIN'),
                    string(credentialsId: 'do-certbot-email', variable: 'CERTBOT_EMAIL'),
                    file(credentialsId:  'do-env-file',       variable: 'PROD_ENV_FILE'),
                    usernamePassword(
                        credentialsId:    'dockerhub-credentials',
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                    )
                ]) {
                    // Step 1: Write the bash deploy script to a local file.
                    // Groovy interpolates: env.REMOTE_DIR, env.DO_DOMAIN, env.CERTBOT_EMAIL, BUILD_NUMBER.
                    // Shell variables use \$ so they are expanded at runtime on the droplet.
                    writeFile file: 'deploy.sh', text: """#!/bin/bash
set -e

REMOTE_DIR="${env.REMOTE_DIR}"
DOMAIN="${env.DO_DOMAIN}"
EMAIL="${env.CERTBOT_EMAIL}"

echo "======================================================"
echo " RentEaseLanka Deploy - Build ${BUILD_NUMBER}"
echo "======================================================"

# Pre-check: verify prerequisites are manually installed
echo "[Pre-check] Verifying Docker, Nginx, and Certbot..."
command -v docker  > /dev/null 2>&1 || { echo "ERROR: Docker not installed. Please install manually.";  exit 1; }
command -v nginx   > /dev/null 2>&1 || { echo "ERROR: Nginx not installed. Please install manually.";   exit 1; }
command -v certbot > /dev/null 2>&1 || { echo "ERROR: Certbot not installed. Please install manually."; exit 1; }
echo "[Pre-check] All prerequisites found."

# [1/4] Prepare deployment directory
echo "[1/4] Preparing deployment directory: \$REMOTE_DIR"
mkdir -p "\$REMOTE_DIR"
cp /tmp/prod.env           "\$REMOTE_DIR/.env"
cp /tmp/docker-compose.yml "\$REMOTE_DIR/docker-compose.yml"

# [2/4] Deploy Nginx config
echo "[2/4] Deploying Nginx config for \$DOMAIN..."
sed "s/DOMAIN_PLACEHOLDER/\$DOMAIN/g" /tmp/renteaselanka.conf > /etc/nginx/sites-available/renteaselanka
ln -sf /etc/nginx/sites-available/renteaselanka /etc/nginx/sites-enabled/renteaselanka
rm -f /etc/nginx/sites-enabled/default
nginx -t

# [3/4] Pull images and restart Docker Compose stack
echo "[3/4] Pulling images and restarting stack..."
echo "\$DOCKER_PASS" | docker login -u "\$DOCKER_USER" --password-stdin
cd "\$REMOTE_DIR"
docker compose pull
docker compose up -d --remove-orphans
docker logout
echo "[3/4] Stack is up."

# [4/4] Obtain or renew SSL certificate via Certbot
echo "[4/4] Running Certbot for HTTPS..."
certbot --nginx --non-interactive --agree-tos --redirect --email "\$EMAIL" -d "\$DOMAIN" -d "www.\$DOMAIN" || echo "[4/4] Certbot skipped (cert already valid)."

# Final Nginx reload
nginx -t && systemctl reload nginx

echo "======================================================"
echo " Deploy complete! https://\$DOMAIN"
echo "======================================================"
"""

                    // Step 2: Upload deploy.sh + env file + compose + nginx config
                    bat """
                        scp -o StrictHostKeyChecking=no -i "%SSH_KEY_FILE%" ^
                            deploy.sh ^
                            "%PROD_ENV_FILE%" ^
                            docker-compose.yml ^
                            nginx/renteaselanka.conf ^
                            %SSH_USER%@%DO_HOST%:/tmp/
                    """

                    // Step 3: Rename env file to prod.env, then run deploy.sh
                    bat """
                        ssh -o StrictHostKeyChecking=no -i "%SSH_KEY_FILE%" ^
                            %SSH_USER%@%DO_HOST% ^
                            "mv /tmp/*env* /tmp/prod.env 2>/dev/null; chmod +x /tmp/deploy.sh && DOCKER_USER=%DOCKER_USER% DOCKER_PASS=%DOCKER_PASS% bash /tmp/deploy.sh"
                    """
                }
            }
        }

        // ── Cleanup local build artifacts ────────────────────────────────
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
            Pipeline completed successfully!
            Backend:  ${DOCKERHUB_USERNAME}/${APP_NAME}-backend:${IMAGE_TAG}
            Frontend: ${DOCKERHUB_USERNAME}/${APP_NAME}-frontend:${IMAGE_TAG}
            =======================================================
            """
        }
        failure {
            echo "Pipeline failed on branch ${env.BRANCH_NAME}"
        }
        always {
            cleanWs()
        }
    }
}