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
                echo "Starting Build #${BUILD_NUMBER} on branch: ${env.BRANCH_NAME ?: 'unknown'}"
            }
        }

        stage('Build Images') {
            steps {
                withCredentials([file(credentialsId: 'renteaselanka_credentials', variable: 'ENV_FILE')]) {
                    bat 'docker compose --env-file "%ENV_FILE%" build'
                }
            }
        }

        stage('Tag Images') {
            steps {
                bat """
                    docker tag ${DOCKERHUB_USERNAME}/${APP_NAME}-backend:latest ${DOCKERHUB_USERNAME}/${APP_NAME}-backend:${IMAGE_TAG}
                    docker tag ${DOCKERHUB_USERNAME}/${APP_NAME}-frontend:latest ${DOCKERHUB_USERNAME}/${APP_NAME}-frontend:${IMAGE_TAG}
                """
            }
        }

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

        stage('Deploy to DigitalOcean') {
            when {
                branch "${DEPLOY_BRANCH}"
            }
            steps {
                withCredentials([
                    sshUserPrivateKey(credentialsId: 'do-ssh-key', keyFileVariable: 'SSH_KEY', usernameVariable: 'SSH_USER'),
                    string(credentialsId: 'do-droplet-host', variable: 'DO_HOST'),
                    string(credentialsId: 'do-domain', variable: 'DO_DOMAIN'),
                    string(credentialsId: 'do-certbot-email', variable: 'CERTBOT_EMAIL'),
                    file(credentialsId: 'do-env-file', variable: 'PROD_ENV_FILE'),
                    usernamePassword(credentialsId: 'dockerhub-credentials', usernameVariable: 'DH_USER', passwordVariable: 'DH_PASS')
                ]) {
                    // 1. Fix SSH key format and Windows permissions
                    writeFile file: 'fix_key.ps1', text: '''
$p = $env:SSH_KEY
$text = [System.IO.File]::ReadAllText($p).Replace("`r`n", "`n")
[System.IO.File]::WriteAllText($p, $text, (New-Object System.Text.UTF8Encoding $false))

$acl = Get-Acl $p
$acl.SetAccessRuleProtection($true, $false)
$rule = New-Object System.Security.AccessControl.FileSystemAccessRule([System.Security.Principal.WindowsIdentity]::GetCurrent().Name, 'Read', 'Allow')
$acl.AddAccessRule($rule)
Set-Acl $p $acl
'''
                    bat 'powershell -ExecutionPolicy Bypass -File fix_key.ps1'

                    // 2. Generate droplet deployment script (no leading indentation on lines)
                    writeFile file: 'remote_deploy.sh', text: """#!/bin/bash
set -e
mkdir -p ${REMOTE_DIR}
cp /tmp/prod.env ${REMOTE_DIR}/.env
cp /tmp/docker-compose.yml ${REMOTE_DIR}/docker-compose.yml

# Nginx config
sed "s/DOMAIN_PLACEHOLDER/${DO_DOMAIN}/g" /tmp/renteaselanka.conf > /etc/nginx/sites-available/renteaselanka
ln -sf /etc/nginx/sites-available/renteaselanka /etc/nginx/sites-enabled/renteaselanka
rm -f /etc/nginx/sites-enabled/default

# Docker pull and start
echo "\$DH_PASS" | docker login -u "\$DH_USER" --password-stdin
cd ${REMOTE_DIR}
docker compose pull
docker compose up -d --remove-orphans
docker logout

# SSL Setup
certbot --nginx --non-interactive --agree-tos --redirect --email "${CERTBOT_EMAIL}" -d "${DO_DOMAIN}" -d "www.${DO_DOMAIN}" || echo "Certbot skipped or already active."
nginx -t && systemctl reload nginx
"""

                    // 3. Upload files to droplet
                    bat """
                        scp -o StrictHostKeyChecking=no -i "%SSH_KEY%" "%PROD_ENV_FILE%" %SSH_USER%@%DO_HOST%:/tmp/prod.env
                        scp -o StrictHostKeyChecking=no -i "%SSH_KEY%" docker-compose.yml nginx/renteaselanka.conf remote_deploy.sh %SSH_USER%@%DO_HOST%:/tmp/
                    """

                    // 4. Run the remote script
                    bat """
                        ssh -o StrictHostKeyChecking=no -i "%SSH_KEY%" %SSH_USER%@%DO_HOST% "DH_USER=%DH_USER% DH_PASS=%DH_PASS% bash /tmp/remote_deploy.sh"
                    """
                }
            }
        }

        stage('Cleanup') {
            steps {
                bat """
                    docker compose down --rmi local 2>nul || exit 0
                    docker rmi ${DOCKERHUB_USERNAME}/${APP_NAME}-backend:${IMAGE_TAG} 2>nul || exit 0
                    docker rmi ${DOCKERHUB_USERNAME}/${APP_NAME}-frontend:${IMAGE_TAG} 2>nul || exit 0
                """
            }
        }
    }

    post {
        success {
            echo "Deployment completed successfully to https://${DO_DOMAIN}"
        }
        failure {
            echo "Build failed on branch: ${env.BRANCH_NAME}"
        }
        always {
            cleanWs()
        }
    }
}