pipeline {
    agent any

    // Подключаем Java 17, настроенную в интерфейсе Jenkins
    tools {
        jdk 'jenkins-jdk17'
    }

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

        // Конфигурация встроенной БД H2 для стабильного запуска в Jenkins
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
                // Стандартная компиляция, использующая Java 17 из блока tools
                sh './gradlew compileJava -x test'
            }
        }

        stage('Build Jar') {
            steps {
                // Сборка Jar-архива без конфликтов с Toolchain
                sh "./gradlew ${params.BUILD_TYPE} -x test"
            }
            post {
                success {
                    archiveArtifacts artifacts: '**/build/libs/*.jar', fingerprint: true
                }
            }
        }

        stage('Run Application') {
            when {
                expression { return params.RUN_STUDENT }
            }
            steps {
                script {
                    // Очистка порта перед запуском с помощью pkill вместо fuser
                    sh "pkill -f spring-0.0.1-SNAPSHOT.jar || true"

                    // Фоновый запуск приложения
                    sh 'nohup java -jar build/libs/spring-0.0.1-SNAPSHOT.jar --server.port=8081 > app_log.txt 2>&1 &'

                    echo "Waiting for app to start with H2 database..."
                    sleep 60

                    // Проверка, что порт отвечает
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
            // Печать логов приложения в общую консоль Jenkins для отладки
            echo "--- APPLICATION LOGS START ---"
            sh "cat app_log.txt || true"
            echo "--- APPLICATION LOGS END ---"
        }
        success {
            echo "🎉 Сборка успешно завершена вручную с параметрами: RUN_STUDENT=${params.RUN_STUDENT}, TYPE=${params.BUILD_TYPE}"
        }
        failure {
            echo '❌ Ошибка сборки.'
        }
    }
}
