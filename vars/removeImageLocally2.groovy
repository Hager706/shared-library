def call(String imageName, String BRANCH_NAME) {
    sh "docker rmi ${imageName}:${BRANCH_NAME}"
}