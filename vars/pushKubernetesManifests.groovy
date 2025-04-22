def call(Map config) {
    if (!config.workDir) {
        error "workDir parameter must be specified for pushManifests"
    }
    
    echo "Pushing changes from directory: ${config.workDir}"
    
    dir(config.workDir) {
        withCredentials([sshUserPrivateKey(
            credentialsId: 'your-ssh-credentials-id',  // Must be SSH type
            keyFileVariable: 'SSH_KEY_FILE',
            usernameVariable: 'SSH_USERNAME'
        )]) {
            sh '''
                # Secure SSH setup
                mkdir -p ~/.ssh
                cp "${SSH_KEY_FILE}" ~/.ssh/git_key
                chmod 600 ~/.ssh/git_key
                
                cat <<EOF > ~/.ssh/config
                Host github.com
                    HostName github.com
                    User git
                    IdentityFile ~/.ssh/git_key
                    StrictHostKeyChecking no
                EOF
                
                # Configure git
                git config user.email "jenkins@example.com"
                git config user.name "Jenkins CI"
                
                # Commit and push
                git add .
                if ! git diff --cached --quiet; then
                    git commit -m "Update manifests for build ${BUILD_NUMBER}"
                    git push origin main
                fi
            '''
        }
    }
}