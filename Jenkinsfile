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
        stage('Checkout') {
            steps {
                checkout scm
                echo "Build: #${BUILD_NUMBER} | Branch: ${env.GIT_BRANCH ?: env.BRANCH_NAME ?: 'unknown'}"
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

        // ── Deploy to DigitalOcean (only on the deploy branch) ──────────
        stage('Deploy to DigitalOcean') {
            when {
                branch "${DEPLOY_BRANCH}"
            }
            steps {
                withCredentials([
                    sshUserPrivateKey(
                        credentialsId : 'do-ssh-key',
                        keyFileVariable: 'SSH_KEY_FILE',
                        usernameVariable: 'SSH_USER'
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
                    // ── 1. Upload deployment files to the droplet ──────────
                    bat """
                        scp -o StrictHostKeyChecking=no -i "%SSH_KEY_FILE%" ^
                            "%PROD_ENV_FILE%" ^
                            docker-compose.yml ^
                            nginx/renteaselanka.conf ^
                            %SSH_USER%@%DO_HOST%:/tmp/
                    """

                    // ── 2. Run remote bootstrap + deploy script ────────────
                    bat """
                        ssh -o StrictHostKeyChecking=no -i "%SSH_KEY_FILE%" ^
                            %SSH_USER%@%DO_HOST% "bash -s" << 'ENDSSH'

set -e

REMOTE_DIR="${REMOTE_DIR}"
DOMAIN="%DO_DOMAIN%"
EMAIL="%CERTBOT_EMAIL%"
DOCKER_USER="%DOCKER_USER%"
DOCKER_PASS="%DOCKER_PASS%"

echo "======================================================"
echo " RentEaseLanka Deploy  —  Build #${BUILD_NUMBER}"
echo "======================================================"

# ── Verify prerequisites are installed ───────────────────────────────
echo "[Pre-check] Verifying Docker, Nginx, and Certbot are installed..."
command -v docker  &> /dev/null || { echo "ERROR: Docker is not installed on the droplet. Please install it manually.";  exit 1; }
command -v nginx   &> /dev/null || { echo "ERROR: Nginx is not installed on the droplet. Please install it manually.";   exit 1; }
command -v certbot &> /dev/null || { echo "ERROR: Certbot is not installed on the droplet. Please install it manually."; exit 1; }
echo "[Pre-check] All prerequisites found."

# ── Prepare remote directory & move uploaded files ──────────────────
echo "[1/4] Preparing deployment directory: \$REMOTE_DIR"
mkdir -p "\$REMOTE_DIR"
cp /tmp/.env*              "\$REMOTE_DIR/.env"             2>/dev/null || cp /tmp/$(basename %PROD_ENV_FILE%) "\$REMOTE_DIR/.env"
cp /tmp/docker-compose.yml "\$REMOTE_DIR/docker-compose.yml"

# ── Deploy Nginx config ──────────────────────────────────────────────
echo "[2/4] Deploying Nginx config for \$DOMAIN ..."
sed "s/DOMAIN_PLACEHOLDER/\$DOMAIN/g" /tmp/renteaselanka.conf \
    > /etc/nginx/sites-available/renteaselanka
ln -sf /etc/nginx/sites-available/renteaselanka /etc/nginx/sites-enabled/renteaselanka
rm -f /etc/nginx/sites-enabled/default
nginx -t

# ── Pull latest images & restart Docker Compose stack ───────────────
echo "[3/4] Pulling images and restarting Docker Compose stack..."
echo "\$DOCKER_PASS" | docker login -u "\$DOCKER_USER" --password-stdin
cd "\$REMOTE_DIR"
docker compose pull
docker compose up -d --remove-orphans
docker logout
echo "[3/4] Stack is up."

# ── Obtain / renew SSL certificate with Certbot ──────────────────────
echo "[4/4] Running Certbot for HTTPS..."
certbot --nginx \
    --non-interactive \
    --agree-tos \
    --redirect \
    --email "\$EMAIL" \
    -d "\$DOMAIN" \
    -d "www.\$DOMAIN" || echo "[4/4] Certbot skipped (cert may already be valid)."

# ── Final Nginx reload ───────────────────────────────────────────────
nginx -t && systemctl reload nginx

echo "======================================================"
echo " Deploy complete!  https://\$DOMAIN"
echo "======================================================"

ENDSSH
                    """
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
            ✅ Pipeline completed successfully!
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