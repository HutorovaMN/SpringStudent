pipeline {
    agent any

    environment {
        APP_PORT = "8081"
        APP_URL  = "http://localhost:${APP_PORT}"

        // Дополнение: переопределяем свойства из application.properties для Jenkins
        // Spring Boot 3.x автоматически поймет, что это H2, по префиксу "jdbc:h2"
        SPRING_DATASOURCE_URL      = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1"
        SPRING_DATASOURCE_USERNAME = "postgres"
        SPRING_DATASOURCE_PASSWORD = "mysecretpassword"
        SPRING_JPA_HIBERNATE_DDL_AUTO = "update"
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
                // Собираем JAR, игнорируя внутренние тесты для ускорения билда
                sh './gradlew clean bootJar -x test'
            }
        }

        stage('Run App Background') {
            steps {
                script {
                    // Очищаем порт перед запуском
                    sh "fuser -k ${APP_PORT}/tcp || true"

                    // Чистый запуск в фоне, переменные окружения подхватятся автоматически
                    sh "nohup java -jar build/libs/*.jar --server.port=${APP_PORT} > app_log.txt 2>&1 &"

                    echo "Waiting for app to start with H2 database..."
                    // Даем Spring Boot 30 секунд на инициализацию контекста и Hibernate
                    sleep 30

                    // Проверяем доступность приложения
                    def r = sh(script: "curl -sI ${APP_URL} | grep HTTP", returnStatus: true)
                    if (r != 0) {
                        error "Приложение не запустилось! Проверьте логи в секции Post Actions."
                    }
                    echo "Application successfully started on port ${APP_PORT}!"
                }
            }
        }

        stage('Run API Tests') {
            steps {
                dir('external-api-tests') {
                    // ОБЯЗАТЕЛЬНО: подставьте ссылку на ваш настоящий репозиторий с API-тестами
                    git url: 'github.com',
                        branch: 'main'

                    sh 'chmod +x gradlew'
                    // Запуск тестов из внешнего репозитория с передачей URL приложения
                    sh "./gradlew test -Dbase.url=${APP_URL}"
                }
            }
        }
    }

    post {
        always {
            // Вывод логов приложения прямо в веб-интерфейс Jenkins для отладки
            echo "--- LOGS START ---"
            sh "cat app_log.txt || true"
            echo "--- LOGS END ---"

            // Сохраняем собранный JAR-артефакт
            archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true, fallbackToNoOp: true

            // Публикуем отчеты о прохождении тестов
            junit allowEmptyResults: true, testResults: '**/build/test-results/test/*.xml'

            // Обязательно освобождаем порт после завершения пайплайна
            sh "fuser -k ${APP_PORT}/tcp || true"
        }
    }
}
