pipeline {
    agent any

    environment {
        SSH_CREDENTIALS_ID = 'ubuntu-ssh'
        SSH_SERVER = '183.102.48.104'
        APP_NAME = 'sms-monitor'
        TARGET_DIR = '/home/mkw111/sms_monitor'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build JAR') {
            steps {
                sh 'chmod +x gradlew'
                sh './gradlew clean build -x test'
            }
        }

        stage('Deploy to Server (Zero-Downtime)') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: "${SSH_CREDENTIALS_ID}", usernameVariable: 'SSH_USER', passwordVariable: 'SSH_PASS')]) {
                        sh '''
                        if ! command -v sshpass >/dev/null 2>&1; then
                            echo "==> Installing sshpass..."
                            (apt-get update && apt-get install -y sshpass) || (sudo apt-get update && sudo apt-get install -y sshpass) || true
                        fi

                        export SSHPASS="$SSH_PASS"
                        TARGET_DIR="/home/mkw111/sms_monitor"
                        APP_NAME="sms-monitor"
                        SERVER="183.102.48.104"

                        echo "==> Preparing build directory on target server..."
                        sshpass -e ssh -o StrictHostKeyChecking=no $SSH_USER@$SERVER "mkdir -p $TARGET_DIR"

                        echo "==> Transferring JAR and Dockerfile to target server..."
                        sshpass -e scp -o StrictHostKeyChecking=no build/libs/*[!plain].jar $SSH_USER@$SERVER:$TARGET_DIR/app.jar
                        sshpass -e scp -o StrictHostKeyChecking=no Dockerfile $SSH_USER@$SERVER:$TARGET_DIR/Dockerfile

                        echo "==> Executing remote Docker build & zero-downtime deployment on server..."
                        sshpass -e ssh -o StrictHostKeyChecking=no $SSH_USER@$SERVER "bash -s" << 'EOF'
set -e
cd /home/mkw111/sms_monitor
APP_NAME="sms-monitor"

echo '==> Building Docker image on target server...'
docker build -t $APP_NAME:latest .

# 1. Determine currently running port (8080 vs 8090)
if curl -s http://127.0.0.1:8080/v3/api-docs >/dev/null 2>&1 || docker ps --filter "publish=8080" -q | grep -q .; then
    CURRENT_PORT=8080
    IDLE_PORT=8090
else
    CURRENT_PORT=8090
    IDLE_PORT=8080
fi

echo "==> Currently active port: $CURRENT_PORT | Target deployment port: $IDLE_PORT"

# 2. Cleanup target container and any process bound to IDLE_PORT
OLD_IDLE_CONTAINERS=$(docker ps -a --filter "publish=$IDLE_PORT" -q)
if [ -n "$OLD_IDLE_CONTAINERS" ]; then
    echo "==> Stopping existing containers bound to port $IDLE_PORT..."
    docker stop $OLD_IDLE_CONTAINERS 2>/dev/null || true
    docker rm $OLD_IDLE_CONTAINERS 2>/dev/null || true
fi

docker stop ${APP_NAME}-$IDLE_PORT 2>/dev/null || true
docker rm ${APP_NAME}-$IDLE_PORT 2>/dev/null || true

# 3. Start new container on IDLE_PORT
echo "==> Starting new container on port $IDLE_PORT..."
docker run -d --name ${APP_NAME}-$IDLE_PORT -p $IDLE_PORT:8080 \
    -e SPRING_PROFILES_ACTIVE=prod \
    -v /home/mkw111/sms_monitor/data:/app/data \
    ${APP_NAME}:latest

# 4. Health Check loop (up to 30 attempts, 2 sec delay)
echo "==> Performing health check on port $IDLE_PORT..."
HEALTHCHECK_SUCCESS=false
for i in $(seq 1 30); do
    HTTP_STATUS=$(curl -o /dev/null -s -w "%{http_code}" http://127.0.0.1:$IDLE_PORT/v3/api-docs || true)
    if [ "$HTTP_STATUS" -eq 200 ] || [ "$HTTP_STATUS" -eq 302 ] || [ "$HTTP_STATUS" -eq 401 ]; then
        echo "==> New container on port $IDLE_PORT is healthy! (HTTP $HTTP_STATUS)"
        HEALTHCHECK_SUCCESS=true
        break
    fi
    echo "Waiting for container on port $IDLE_PORT... ($i/30)"
    sleep 2
done

if [ "$HEALTHCHECK_SUCCESS" = "false" ]; then
    echo "==> [ERROR] Health check failed! Aborting..."
    docker stop ${APP_NAME}-$IDLE_PORT 2>/dev/null || true
    docker rm ${APP_NAME}-$IDLE_PORT 2>/dev/null || true
    exit 1
fi

# 5. Switch Nginx traffic to IDLE_PORT
if [ -f /etc/nginx/sites-available/default ]; then
    sudo sed -i "s/localhost:$CURRENT_PORT/localhost:$IDLE_PORT/g" /etc/nginx/sites-available/default 2>/dev/null || true
    sudo systemctl reload nginx 2>/dev/null || sudo nginx -s reload 2>/dev/null || true
    echo "==> Nginx reloaded to point to port $IDLE_PORT"
fi

# 6. Stop old container after successful traffic switch
echo "==> Stopping old container on port $CURRENT_PORT..."
docker stop ${APP_NAME}-$CURRENT_PORT 2>/dev/null || true
docker rm ${APP_NAME}-$CURRENT_PORT 2>/dev/null || true

echo "==> Zero-Downtime Deployment Succeeded!"
EOF
                        '''
                    }
                }
            }
        }
    }

    post {
        success {
            echo 'SmsMonitor Backend Deployment Succeeded!'
        }
        failure {
            echo 'SmsMonitor Backend Deployment Failed.'
        }
    }
}

