def call(String imageName, String BRANCH_NAME) {
    sh "docker build -t ${imageName}:${BRANCH_NAME} ."
}

