def call(String imageName, String BRANCH_NAME, String credentialsId) {
    withCredentials([usernamePassword(credentialsId: credentialsId, usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
        sh "docker login -u ${USERNAME} -p ${PASSWORD}"
        sh "docker push ${imageName}:${BRANCH_NAME}"
    }
}