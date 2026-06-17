pipeline {
    agent any

    tools {
        jdk 'JDK17'
        maven 'Maven3'
    }

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    parameters {
        booleanParam(name: 'SKIP_TESTS', defaultValue: true, description: 'Skip tests during CI build.')
        booleanParam(name: 'DEPLOY_TO_NEXUS', defaultValue: false, description: 'Deploy Maven artifact to Nexus.')
        booleanParam(name: 'DEPLOY_DOCKER', defaultValue: false, description: 'Build Docker image and restart the app container.')
        string(name: 'DOCKER_IMAGE_NAME', defaultValue: 'lingolens', description: 'Docker image name used on the Jenkins agent.')
        string(name: 'DOCKER_HOST_PORT', defaultValue: '18080', description: 'Host port mapped to container port 8080.')
    }

    environment {
        MAVEN_REPO_LOCAL = '.m2/repository'
        NEXUS_CREDENTIALS_ID = 'nexus-admin'
        LLM_AUTHORIZATION_CREDENTIALS_ID = 'lingolens-llm-authorization'
        APP_CONTAINER_NAME = 'lingolens-app'
        APP_CONTAINER_PORT = '8080'
    }

    stages {
        stage('Checkout Code') {
            steps {
                echo 'Checking out LingoLens source code...'
                checkout([$class: 'GitSCM',
                    branches: [[name: '*/main']],
                    extensions: [],
                    userRemoteConfigs: [[
                        credentialsId: 'git-ssh-auth',
                        url: 'git@github.com:hatsune-hitsuzi/LingoLens.git'
                    ]]
                ])
            }
        }

        stage('Maven Build') {
            steps {
                echo 'Building with JDK17 and Maven3...'
                script {
                    mvn("clean package ${testFlag()}")
                }
            }
        }

        stage('Archive Artifact') {
            steps {
                echo 'Archiving build artifacts...'
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }

        stage('Deploy to Nexus') {
            when {
                expression { return params.DEPLOY_TO_NEXUS }
            }
            steps {
                echo 'Deploying Maven artifact to Nexus...'
                withCredentials([usernamePassword(
                        credentialsId: env.NEXUS_CREDENTIALS_ID,
                        usernameVariable: 'NEXUS_USERNAME',
                        passwordVariable: 'NEXUS_PASSWORD'
                )]) {
                    script {
                        writeMavenSettings()
                        mvn("deploy ${testFlag()} -Dspring-boot.repackage.skip=true -s settings-ci.xml")
                    }
                }
            }
            post {
                always {
                    script {
                        removeFile('settings-ci.xml')
                    }
                }
            }
        }

        stage('Docker Build') {
            when {
                expression { return params.DEPLOY_DOCKER }
            }
            steps {
                echo 'Building Docker image...'
                script {
                    dockerBuild()
                }
            }
        }

        stage('Docker Deploy') {
            when {
                expression { return params.DEPLOY_DOCKER }
            }
            steps {
                echo 'Deploying Docker container...'
                script {
                    dockerDeploy()
                }
            }
        }
    }

    post {
        success {
            echo 'LingoLens pipeline finished successfully.'
        }
        failure {
            echo 'LingoLens pipeline failed. Check the stage logs above.'
        }
    }
}

String testFlag() {
    return params.SKIP_TESTS ? '-DskipTests' : ''
}

void mvn(String goals) {
    String command = "mvn -B -U -Dmaven.repo.local=${env.MAVEN_REPO_LOCAL} ${goals}".trim()
    if (isUnix()) {
        sh command
    } else {
        bat command
    }
}

void writeMavenSettings() {
    writeFile file: 'settings-ci.xml', text: """<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 https://maven.apache.org/xsd/settings-1.0.0.xsd">
    <servers>
        <server>
            <id>nexus-releases</id>
            <username>${env.NEXUS_USERNAME}</username>
            <password>${env.NEXUS_PASSWORD}</password>
        </server>
        <server>
            <id>nexus-snapshots</id>
            <username>${env.NEXUS_USERNAME}</username>
            <password>${env.NEXUS_PASSWORD}</password>
        </server>
    </servers>
</settings>
"""
}

void removeFile(String path) {
    if (isUnix()) {
        sh "rm -f ${path}"
    } else {
        bat "if exist ${path} del /f /q ${path}"
    }
}

String dockerImageTag() {
    return "${dockerImageName()}:${env.BUILD_NUMBER}"
}

String dockerImageName() {
    return params.DOCKER_IMAGE_NAME.trim().toLowerCase()
}

String dockerHostPort() {
    return params.DOCKER_HOST_PORT.trim()
}

void validateDockerConfig() {
    String imageName = dockerImageName()
    String hostPort = dockerHostPort()
    if (!(imageName ==~ /^[a-z0-9][a-z0-9._\/-]{0,127}$/)) {
        error("Invalid DOCKER_IMAGE_NAME: ${imageName}")
    }
    if (!(hostPort ==~ /^[0-9]{1,5}$/)) {
        error("Invalid DOCKER_HOST_PORT: ${hostPort}")
    }
    Integer port = hostPort as Integer
    if (port < 1 || port > 65535) {
        error("DOCKER_HOST_PORT is out of range: ${hostPort}")
    }
}

void dockerBuild() {
    if (!isUnix()) {
        error('Docker deploy requires a Linux Jenkins agent.')
    }
    validateDockerConfig()
    String image = dockerImageTag()
    String imageName = dockerImageName()
    sh """
        docker build --pull -t ${image} -t ${imageName}:latest .
    """
}

void dockerDeploy() {
    if (!isUnix()) {
        error('Docker deploy requires a Linux Jenkins agent.')
    }
    validateDockerConfig()
    String image = dockerImageTag()
    String hostPort = dockerHostPort()
    withCredentials([string(
            credentialsId: env.LLM_AUTHORIZATION_CREDENTIALS_ID,
            variable: 'LINGOLENS_LLM_AUTHORIZATION'
    )]) {
        sh """
            docker stop ${env.APP_CONTAINER_NAME} >/dev/null 2>&1 || true
            docker rm ${env.APP_CONTAINER_NAME} >/dev/null 2>&1 || true
            docker run -d \\
                --name ${env.APP_CONTAINER_NAME} \\
                --restart unless-stopped \\
                -p ${hostPort}:${env.APP_CONTAINER_PORT} \\
                -e TZ=Asia/Shanghai \\
                -e LINGOLENS_LLM_ENABLED=true \\
                -e LINGOLENS_LLM_AUTHORIZATION="\$LINGOLENS_LLM_AUTHORIZATION" \\
                ${image}
            docker ps --filter "name=${env.APP_CONTAINER_NAME}"
        """
    }
}
