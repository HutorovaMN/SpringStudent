pipeline {
    agent any

    environment {
        // Порт 8081, чтобы не было конфликта с самим Jenkins (8080)
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
                    // Убиваем старый процесс, если он висит на этом порту
                    sh "fuser -k ${APP_PORT}/tcp || true"

                    // Запуск приложения в фоне (nohup + &)
                    // Логи будут писаться в app_log.txt
                    sh "nohup java -jar build/libs/*.jar --server.port=${APP_PORT} > app_log.txt 2>&1 &"

                    // Ждем, пока Spring Boot поднимется (Health check)
                    echo "Waiting for app to start on ${APP_URL}..."
                    timeout(time: 2, unit: 'MINUTES') {
                        waitUntil {
                            def r = sh(script: "curl -s ${APP_URL}/actuator/health | grep UP", returnStatus: true)
                            return (r == 0)
                        }
                    }
                    echo "Application is UP!"
                }
            }
        }

        stage('Run API Tests from External Repo') {
            steps {
                // Создаем отдельную папку для тестов
                dir('external-api-tests') {
                    // Клонируем репозиторий с тестами
                    git url: 'https://github.com',
                        branch: 'main'

                    sh 'chmod +x gradlew'
                    // Запускаем тесты и передаем URL нашего запущенного приложения
                    sh "./gradlew test -Dbase.url=${APP_URL}"
                }
            }
        }
    }

    post {
        always {
            // Собираем артефакты и отчеты
            archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true

            // Если в репозитории с тестами настроен Allure
            allure includeProperties: false, results: [[path: 'external-api-tests/build/allure-results']]

            // Останавливаем приложение после тестов
            sh "fuser -k ${APP_PORT}/tcp || true"
        }
        failure {
            echo "Pipeline failed. Check app_log.txt for details."
            // Можно вывести логи приложения в консоль Jenkins при падении
            sh "cat app_log.txt || true"
        }
    }
}
