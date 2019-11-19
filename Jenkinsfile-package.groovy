pipeline {
    agent any
    tools {
        maven 'M3'
    }
    environment {
        branch_name = ""
        mvn_profile = ""
    }
    stages {
        stage('Checkout') {
            steps {
                script {
                    // jobName 格式: auto*.?-BRANCH_NAME,PROJECT_NAME
                    jobName = "${env.JOB_NAME}".replaceAll("auto*.?-", "")
                    str = jobName.split(",")
                    branch_name = str[0]
                    // maven配置
                    if (branch_name == "test_83") {
                        mvn_profile = "test83"
                    } else if (branch_name == "test_206") {
                        mvn_profile = "test206"
                    } else if (branch_name == "zsc") {
                        mvn_profile = "zsc"
                    } else if (branch_name == "master") {
                        mvn_profile = "pro"
                    }
                    def projects = [
                            'dianchou-common',
                            'dianchou-about',
                            'dianchou-account',
                            'dianchou-activity',
                            'dianchou-address',
                            'dianchou-adminuser',
                            'dianchou-audit',
                            'dianchou-chat',
                            'dianchou-data_migration',
                            'dianchou-dianChouBox',
                            'dianchou-dictionary',
                            'dianchou-exceptionHandler',
                            'dianchou-finance',
                            'dianchou-fresh',
                            'dianchou-ipush',
                            'dianchou-log',
                            'dianchou-pay',
                            'dianchou-proFarmShop',
                            'dianchou-project',
                            'dianchou-quartz',
                            'dianchou-search',
                            'dianchou-shop',
                            'dianchou-third_system',
                            'dianchou-user',
                            'dianchou-read',
                            'dianchou-productOrder',
                            'dianchou-proFarmShop',
                            'dianchou-dianChouBox',
                            'dianchou-productFarmer',
                            'dianchou-productProject',
                            'dianchou-productPublic'
                    ]
                    for (int i = 0; i < projects.size(); ++i) {
                        project = projects[i]
                        sh "mkdir -p ${project}"
                        dir(project) {
                            git branch: "${branch_name}", changelog: false, credentialsId: 'git', url: "ssh://git@gitlab.tdianchou.com:4422/chenchao/${project}.git"
                        }
                    }
                }
            }
        }
        stage('Package') {
            steps {
                sh "mvn clean install -f dianchou-common/dianchou-common-parent -N"
                sh "mvn clean install -q -f dianchou-common/dianchou-common-package -P ${mvn_profile}"
            }
        }
    }
}
