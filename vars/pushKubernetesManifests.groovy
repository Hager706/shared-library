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
            // Write the SSH command to a variable safely
            wrap([$class: 'MaskPasswordsBuildWrapper']) {
                sh '''
                    # Securely setup SSH without string interpolation
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
                    git config user.email "jenkins@yourcompany.com"
                    git config user.name "Jenkins CI"
                    
                    # Stage and commit changes
                    git add .
                    
                    if ! git diff --cached --quiet; then
                        git commit -m "Update manifests for build ${BUILD_NUMBER}"
                        
                        # Push using SSH
                        git remote set-url origin git@github.com:Hager706/kubernetes-manifests.git
                        git push origin main
                    else
                        echo "No changes to commit"
                    fi
                '''
            }
        }
    }
}