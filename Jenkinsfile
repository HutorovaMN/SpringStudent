// pipeline {
//     agent any
//
//     parameters {
//         booleanParam(
//             name: 'RUN_STUDENT',
//             defaultValue: true,
//             description: 'Запускать приложение после сборки?'
//         )
//         choice(
//             name: 'BUILD_TYPE',
//             choices: ['bootJar', 'jar'],
//             description: 'Тип собираемого архива (bootJar для Spring Boot)'
//         )
//     }
//
//     environment {
//         GRADLE_USER_HOME = "${WORKSPACE}/.gradle"
//         APP_PORT = "3030"
//         APP_URL  = "http://localhost:${APP_PORT}"
//
//         SPRING_DATASOURCE_URL      = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1"
//         SPRING_DATASOURCE_USERNAME = "sa"
//         SPRING_DATASOURCE_PASSWORD = ""
//         SPRING_JPA_HIBERNATE_DDL_AUTO = "update"
//     }
//
//     stages {
//         stage('Checkout') {
//             steps {
//                 checkout scm
//             }
//         }
//
//         stage('Grant Execute Permissions') {
//             steps {
//                 sh 'chmod +x ./gradlew'
//             }
//         }
//
//         stage('Compile') {
//             steps {
//                 // Разрешаем Gradle скачивать JDK автоматически, если локальной версии нет
//                 sh './gradlew compileJava -x test -Porg.gradle.java.installations.auto-download=true'
//             }
//         }
//
//         stage('Build Jar') {
//             steps {
//                 sh "./gradlew ${params.BUILD_TYPE} -x test -Porg.gradle.java.installations.auto-download=true"
//             }
//             post {
//                 success {
//                     archiveArtifacts artifacts: '**/build/libs/*.jar', fingerprint: true
//                 }
//             }
//         }
//
//         stage('Run Application') {
//             when {
//                 expression { return params.RUN_STUDENT }
//             }
//             steps {
//                 script {
//                     sh "pkill -f spring-0.0.1-SNAPSHOT.jar || true"
//                     sh 'nohup java -jar build/libs/spring-0.0.1-SNAPSHOT.jar --server.port=3030 > app_log.txt 2>&1 &'
//
//                     echo "Waiting for app to start with H2 database..."
//                     sleep 60
//
//                     def r = sh(script: "curl -sI ${APP_URL} | grep HTTP", returnStatus: true)
//                     if (r != 0) {
//                         error "Приложение не поднялось. Проверьте логи ниже."
//                     }
//                     echo "Application successfully started and running on port 3030!"
//                 }
//             }
//         }
//     }
//
//     post {
//         always {
//             echo "--- APPLICATION LOGS START ---"
//             sh "cat app_log.txt || true"
//             echo "--- APPLICATION LOGS END ---"
//         }
//         success {
//             echo "🎉 Сборка успешно завершена вручную с параметрами: RUN_STUDENT=${params.RUN_STUDENT}, TYPE=${params.BUILD_TYPE}"
//         }
//         failure {
//             echo '❌ Ошибка сборки.'
//         }
//     }
// }

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
        // Порт внутри контейнера приложения останется 3030
        APP_PORT = "3030"

        SPRING_DATASOURCE_URL      = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1"
        SPRING_DATASOURCE_USERNAME = "sa"
        SPRING_DATASOURCE_PASSWORD = ""
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
                sh './gradlew compileJava -x test -Porg.gradle.java.installations.auto-download=true'
            }
        }

        stage('Build Jar') {
            steps {
                sh "./gradlew ${params.BUILD_TYPE} -x test -Porg.gradle.java.installations.auto-download=true"
            }
            post {
                success {
                    archiveArtifacts artifacts: '**/build/libs/*.jar', fingerprint: true
                }
            }
        }

        stage('Run Application') {
            steps {
                script {
                    // Используем корректный Groovy-синтаксис 'if' вместо декларативного 'when'
                    if (params.RUN_STUDENT) {
                        echo "Stopping old container if exists..."
                        sh 'docker stop student-app || true'
                        sh 'docker rm student-app || true'

                        echo "Creating Dockerfile..."
                        sh '''
                        cat <<EOF > Dockerfile
                        FROM eclipse-temurin:21-jre-jammy
                        COPY build/libs/spring-0.0.1-SNAPSHOT.jar app.jar
                        EXPOSE 3030
                        ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=3030"]
                        EOF
                        '''

                        echo "Building Docker Image..."
                        sh 'docker build -t student-app:latest .'

                        echo "Starting Docker Container on host port 8081..."
                        // Пробрасываем внутренний порт 3030 на внешний порт 8081 хост-машины
                        sh 'docker run -d --name student-app -p 8081:3030 student-app:latest'

                        echo "Waiting for app to initialize..."
                        sleep 30

                        echo "Application successfully started and isolated in container!"
                    } else {
                        echo "Запуск приложения пропущен пользователем (RUN_STUDENT = false)"
                    }
                }
            }
        }
    }

    post {
        always {
            echo "--- PIPELINE FINISHED ---"
        }
        success {
            echo "🎉 Сборка успешно завершена с параметрами: RUN_STUDENT=${params.RUN_STUDENT}, TYPE=${params.BUILD_TYPE}"
            echo "Если RUN_STUDENT=true, приложение доступно по адресу: http://localhost:8081"
        }
        failure {
            echo '❌ Ошибка сборки. Проверьте шаги выше.'
        }
    }
}

