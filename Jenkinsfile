pipeline {
    agent any

    parameters {
        booleanParam(
            name: 'RUN_STUDENT',
            defaultValue: true,
            description: 'Запускать приложение после сборки?'
        )
        choice(
            name: 'BUILD_TYPE',
            choices: ['bootJar', 'jar'],
            description: 'Тип собираемого архива (bootJar для Spring Boot)'
        )
    }

    environment {
        GRADLE_USER_HOME = "${WORKSPACE}/.gradle"
        APP_PORT = "8081"
        APP_URL  = "http://localhost:${APP_PORT}"

        // Встраиваем H2 базу, чтобы приложение успешно запустилось в Jenkins
        SPRING_DATASOURCE_URL      = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1"
        SPRING_DATASOURCE_USERNAME = "postgres"
        SPRING_DATASOURCE_PASSWORD = "mysecretpassword"
        SPRING_JPA_HIBERNATE_DDL_AUTO = "update"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Grant Execute Permissions') {
            steps {
                sh 'chmod +x ./gradlew'
            }
        }

        stage('Compile') {
            steps {
                sh './gradlew compileJava -x test'
            }
        }

        stage('Build Jar') {
            steps {
                sh "./gradlew ${params.BUILD_TYPE} -x test"
            }
            post {
                success {
                    archiveArtifacts artifacts: '**/build/libs/*.jar', fingerprint: true
                }
            }
        }

        // Новая стадия: запуск приложения в фоне без внешних тестов
        stage('Run Application') {
            when {
                expression { return params.RUN_STUDENT }
            }
            steps {
                script {
                    // Убиваем старый процесс, если он остался от прошлых запусков
                    sh "fuser -k ${APP_PORT}/tcp || true"

                    // Запускаем приложение в фоновом режиме
                    sh "nohup java -jar build/libs/*.jar --server.port=${APP_PORT} > app_log.txt 2>&1 &"

                    // ВСТАВИЛ СТРОКУ СЮДА
                    echo "Waiting for app to start with H2 database..."
                    sleep 30

                    // Проверяем, жива ли сборка
                    def r = sh(script: "curl -sI ${APP_URL} | grep HTTP", returnStatus: true)
                    if (r != 0) {
                        error "Приложение не поднялось. Проверьте логи ниже."
                    }
                    echo "Application successfully started and running on port ${APP_PORT}!"
                }
            }
        }
    }

    post {
        always {
            // Выводим логи приложения прямо в консоль сборки для отладки
            echo "--- APPLICATION LOGS START ---"
            sh "cat app_log.txt || true"
            echo "--- APPLICATION LOGS END ---"

            // Внимание: команда fuser отсюда убрана, чтобы приложение ОСТАЛОСЬ работать
        }
        success {
            echo "🎉 Сборка успешно завершена вручную с параметрами: RUN_STUDENT=${params.RUN_STUDENT}, TYPE=${params.BUILD_TYPE}"
        }
        failure {
            echo '❌ Ошибка сборки.'
        }
    }
}
