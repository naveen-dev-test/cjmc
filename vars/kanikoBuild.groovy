// vars/kanikoBuild.groovy

def call(Map config) {
    pipeline {
        agent any
        environment {
            DOCKER_IMAGE = config.dockerImage // Set the image name from parameters
            DOCKER_REGISTRY = config.dockerRegistry // Set the registry URL from parameters
            DOCKER_REGISTRY_CREDENTIALS = config.dockerRegistryCredentials // Credentials ID for Docker registry
        }
        stages {
            stage('Kaniko Multi-Arch Build') {
                steps {
                    container('kaniko') { // Using a Kaniko container for the build
                        script {
                            withCredentials([string(credentialsId: DOCKER_REGISTRY_CREDENTIALS, variable: 'DOCKER_REGISTRY_TOKEN')]) {
                                sh """
                                /kaniko/executor \
                                    --context ${config.context ?: '.'} \
                                    --dockerfile ${config.dockerfile ?: 'Dockerfile'} \
                                    --destination ${DOCKER_REGISTRY}/${DOCKER_IMAGE}:latest \
                                    --destination ${DOCKER_REGISTRY}/${DOCKER_IMAGE}:amd64 \
                                    --destination ${DOCKER_REGISTRY}/${DOCKER_IMAGE}:arm64 \
                                    --build-arg BUILDSLAVE_PASSWORD=buildslave \
                                    --platform=linux/amd64,linux/arm64
                                """
                            }
                        }
                    }
                }
            }
        }
    }
}

