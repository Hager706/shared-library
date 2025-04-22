#!/usr/bin/env groovy

def call(Map config) {
    // Make sure workDir is specified
    def workDir = config.workDir
    
    if (workDir == null || workDir.trim() == "") {
        error "workDir parameter must be specified for pushManifests"
        return
    }
    
    echo "Pushing changes from directory: ${workDir}"
    
    dir(workDir) {
        // Check credential type and use appropriate method
        if (config.credentialType == 'ssh') {
            // For SSH credentials
            withCredentials([sshUserPrivateKey(credentialsId: config.credentialsId, 
                                              keyFileVariable: 'SSH_KEY_FILE', 
                                              usernameVariable: 'SSH_USERNAME')]) {
                sh """
                    # Setup Git SSH with the private key
                    export GIT_SSH_COMMAND="ssh -i ${SSH_KEY_FILE} -o StrictHostKeyChecking=no"
                    
                    # Commit changes
                    git commit -m "Update image tag for build #${env.BUILD_NUMBER}" || echo "No changes to commit"
                    
                    # Push changes using SSH
                    git push origin main || echo "Push failed - possibly no changes to push"
                """
            }
        } else {
            // For HTTPS, use username/password credentials
            withCredentials([usernamePassword(credentialsId: config.credentialsId, 
                                             passwordVariable: 'GIT_PASSWORD', 
                                             usernameVariable: 'GIT_USERNAME')]) {
                sh """
                    # Commit changes
                    git commit -m "Update image tag for build #${env.BUILD_NUMBER}" || echo "No changes to commit"
                    
                    # Configure Git credential helper
                    git config --local credential.helper '!f() { echo "username=${GIT_USERNAME}"; echo "password=${GIT_PASSWORD}"; }; f'
                    
                    # Push changes
                    git push origin main || echo "Push failed - possibly no changes to push"
                """
            }
        }
    }
}