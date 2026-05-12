pipeline {
    agent any

    environment {
        APP_PORT = "8081"
        APP_URL = "http://localhost:${APP_PORT}"
    }

    stages {
        stage('Checkout App') {
            steps {
                checkout scm
            }
        }

        stage('Build App') {
            steps {
                sh 'chmod +x gradlew'
                sh './gradlew clean bootJar -x test'
            }
        }

        stage('Run App Background') {
            steps {
                script {
                    // Убиваем старый процесс на порту
                    sh "fuser -k ${APP_PORT}/tcp || true"

                    // Запуск приложения
                    sh "nohup java -jar build/libs/*.jar --server.port=${APP_PORT} > app_log.txt 2>&1 &"

                    echo "Waiting for app to start..."
                    // Ждем 30 секунд (даем время Spring Boot подняться без Actuator)
                    sleep 30

                    // Проверяем, что приложение отвечает хоть чем-то (даже 404)
                    def r = sh(script: "curl -sI ${APP_URL} | grep HTTP", returnStatus: true)
                    if (r != 0) {
                        error "Приложение не запустилось! Проверьте логи ниже."
                    }
                }
            }
        }

        stage('Run API Tests') {
            steps {
                dir('external-api-tests') {
                    // ОБЯЗАТЕЛЬНО: укажите здесь ваш реальный репозиторий с тестами
                    git url: 'https://github.com',
                        branch: 'main'

                    sh 'chmod +x gradlew'
                    sh "./gradlew test -Dbase.url=${APP_URL}"
                }
            }
        }
    }

    post {
        always {
            // Выводим логи приложения в консоль Jenkins, чтобы видеть ошибки базы данных
            echo "--- LOGS START ---"
            sh "cat app_log.txt || true"
            echo "--- LOGS END ---"

            archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true

            // Удаляем allure, чтобы не было ошибки NoSuchMethodError
            junit '**/build/test-results/test/*.xml'

            sh "fuser -k ${APP_PORT}/tcp || true"
        }
    }
}
