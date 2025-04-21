#!/usr/bin/env groovy

def call(Map config) {
    def manifestsRepo = config.manifestsRepo
    def imageName = config.imageName
    def imageTag = config.imageTag
    def appName = config.appName
    def deploymentFile = config.deploymentFile ?: "deployment.yaml"
    
    def workDir = "k8s-manifests-${UUID.randomUUID().toString()}"
    
    dir(workDir) {
        // Checkout the public repository without credentials
        checkout([
            $class: 'GitSCM',
            branches: [[name: 'main']],
            userRemoteConfigs: [[
                url: manifestsRepo
            ]]
        ])
        
        sh """
            # Create and checkout a local main branch that tracks origin/main
            git checkout -B main origin/main
            
            # Update the image tag in the deployment file
            sed -i "s|image: ${imageName}:.*|image: ${imageName}:${imageTag}|g" ${deploymentFile}
            
            # Verify the change
            grep -n "image: ${imageName}" ${deploymentFile}
            
            # Configure Git user
            git config user.email "jenkins@ivolve.com"
            git config user.name "Jenkins Pipeline"
            
            # Stage the changes
            git add ${deploymentFile}
            
            # Commit the changes
            git commit -m "Update ${appName} image to ${imageTag}"
            
            # Push the changes back to the repository
            # Note: This will fail unless you've configured write access
            git push origin main
        """
    }
    
    // Return the directory path for the next stage
    return workDir
}