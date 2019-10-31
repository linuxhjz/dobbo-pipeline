pipeline {
    agent any
    tools {
        maven 'M3'
    }
    environment {
        branch_name = ''
        mvn_profile = ''
        projectName = ''
        buildType = ''
        shortName = ''
    }
    stages {
        stage('Checkout') {
            steps {
                script {
                    // jobName 格式: auto*.?-BRANCH_NAME,PROJECT_NAME
                    jobName = "${env.JOB_NAME}".replaceAll("auto*.?-", "")
                    str = jobName.split(",")
                    branch_name = str[0]
                    projectName = str[1]
                    // maven配置
                    if (branch_name == "test_83") {
                        mvn_profile = "test83"
                    } else if (branch_name == "test_206") {
                        mvn_profile = "test"
                    } else if (branch_name == "zsc") {
                        mvn_profile = "zsc"
                    } else if (branch_name == "master") {
                        mvn_profile = "pro"
                    }
                    if (projectName.contains('boss')) {
                        buildType = 'boss'
                    } else {
                        buildType = 'service'
                    }
                    shortName = projectName.replaceAll('dianchou-', '')
                }
                git branch: "${branch_name}", credentialsId: 'git', url: "git@47.97.45.82:chenchao/${projectName}.git"
            }
        }
        stage('Package') {
            steps {
                script {
                    if (buildType == 'common') {
                        sh "mvn clean install -f dianchou-common-package -P ${mvn_profile}"
                    } else if (buildType == 'service') {
                        sh "mvn clean package -f dianchou-service-${shortName}"
                    } else if (buildType == 'boss') {
                        sh "mvn clean package"
                    }
                }
            }
        }
        stage('Deploy') {
            steps {
                script {
                    if (buildType == 'service') {
                        sh "mkdir -p /usr/dianchou/service/${shortName}/lib"
                        sh "\\cp -f /var/lib/jenkins/workspace/${env.JOB_NAME}/dianchou-service-${shortName}/target/*.jar /usr/dianchou/service/${shortName}"
                        sh "\\cp -rf /var/lib/jenkins/workspace/${env.JOB_NAME}/dianchou-service-${shortName}/target/lib/* /usr/dianchou/service/${shortName}/lib"
                        sh "/etc/init.d/jdk_start ${shortName}"
                    } else if (buildType == 'boss') {
                        sh "\\rm -rf /usr/dianchou/tomcat/${projectName}/WEB-INF"
                        sh "\\cp -rf /var/lib/jenkins/workspace/${env.JOB_NAME}/target/${projectName}/* /usr/dianchou/tomcat/${projectName}"
                        sh "/usr/java/tomcat/${projectName}/bin/dianchou_service.sh"
                    }
                }
            }
        }
    }
}
