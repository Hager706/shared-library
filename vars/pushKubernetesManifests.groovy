def call(Map config) {
    // Validate required parameters
    if (!config.workDir) {
        error "workDir parameter must be specified for pushManifests"
    }
    
    echo "Pushing changes from directory: ${config.workDir}"
    
    dir(config.workDir) {
        withCredentials([sshUserPrivateKey(
            credentialsId: config.credentialsId, 
            keyFileVariable: 'SSH_KEY_FILE', 
            usernameVariable: 'SSH_USERNAME'
        )]) {
            sh """
                # Setup Git SSH with the private key
                export GIT_SSH_COMMAND="ssh -i ${SSH_KEY_FILE} -o StrictHostKeyChecking=no"
                
                # Configure git user
                git config user.email "jenkins@yourcompany.com"
                git config user.name "Jenkins CI"
                
                # Stage all changes
                git add .
                
                # Check if there are changes to commit
                if ! git diff --cached --quiet; then
                    git commit -m "Update manifests for build ${env.BUILD_NUMBER}"
                    
                    # Push changes to main branch
                    git push origin main
                else
                    echo "No changes to commit"
                fi
            """
        }
    }
}