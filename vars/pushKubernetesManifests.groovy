#!/usr/bin/env groovy

def call(Map config) {
    def credentialsId = config.credentialsId
    def workDir = config.workDir
    
    dir(workDir) {
        // For HTTPS, use username/password credentials
        withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
            sh """
                # Commit changes
                git commit -m "Update image tag for build #${env.BUILD_NUMBER}" || echo "No changes to commit"
                
                # Set up credential helper for HTTPS
                git config --local credential.helper '!f() { echo "username=\${GIT_USERNAME}"; echo "password=\${GIT_PASSWORD}"; }; f'
                
                # Push changes
                git push origin main
            """
        }
        
        // Alternative method for SSH if preferred
        // Uncomment this and comment out the above withCredentials block if you want to use SSH
        /*
        withCredentials([sshUserPrivateKey(credentialsId: credentialsId, keyFileVariable: 'SSH_KEY')]) {
            sh """
                # Commit changes
                git commit -m "Update image tag for build #${env.BUILD_NUMBER}" || echo "No changes to commit"
                
                # Use SSH key with StrictHostKeyChecking disabled
                GIT_SSH_COMMAND="ssh -i \${SSH_KEY} -o StrictHostKeyChecking=no" git push origin main
            """
        }
        */
    }
}