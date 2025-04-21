#!/usr/bin/env groovy

def call(Map config) {
    def manifestsRepo = config.manifestsRepo
    def credentialsId = config.credentialsId
    def workDir = config.workDir ?: "k8s-manifests-${UUID.randomUUID().toString()}"
    
    dir(workDir) {
        withCredentials([sshUserPrivateKey(credentialsId: credentialsId, keyFileVariable: 'SSH_KEY')]) {
            sh """
                # Commit and push changes
                git commit -m "Update image tag for build #${env.BUILD_NUMBER}" || echo "No changes to commit"
                
                # Use SSH key for push
                GIT_SSH_COMMAND="ssh -i ${SSH_KEY} -o StrictHostKeyChecking=no" git push origin main
            """
        }
    }
}