def call(Map config) {
    def manifestsRepo = config.manifestsRepo
    def credentialsId = config.credentialsId
    def imageName = config.imageName
    def imageTag = config.imageTag
    def appName = config.appName
    def deploymentFile = config.deploymentFile ?: "deployment.yaml"
    
    def workDir = "k8s-manifests-${UUID.randomUUID().toString()}"
    
    dir(workDir) {
        // Checkout using SSH credentials
        checkout([
            $class: 'GitSCM',
            branches: [[name: 'main']],
            extensions: [[
                $class: 'CleanBeforeCheckout'
            ]],
            userRemoteConfigs: [[
                url: manifestsRepo,
                credentialsId: credentialsId
            ]]
        ])
        
        sh """
            # Set up local branch
            git checkout -B main origin/main
            
            # Update image tag
            sed -i "s|image: ${imageName}:.*|image: ${imageName}:${imageTag}|g" ${deploymentFile}
            
            # Verify change
            grep -n "image: ${imageName}" ${deploymentFile}
            
            # Configure Git
            git config user.email "jenkins@example.com"
            git config user.name "Jenkins"
            
            # Commit changes
            git add ${deploymentFile}
            git commit -m "Update ${appName} image to ${imageTag}"
            
            # Push changes using the same SSH credentials
            git push origin main
        """
    }
    
    return workDir
}