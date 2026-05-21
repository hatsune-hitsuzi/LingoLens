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
    }

    environment {
        MAVEN_REPO_LOCAL = '.m2/repository'
        NEXUS_CREDENTIALS_ID = 'nexus-admin'
    }

    stages {
        stage('Checkout Code') {
            steps {
                echo 'Checking out source code...'
                checkout scm
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
