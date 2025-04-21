#!/usr/bin/env groovy

def call(Map config) {
    def manifestsRepo = config.manifestsRepo
    def credentialsId = config.credentialsId
    def imageName = config.imageName
    def imageTag = config.imageTag
    def appName = config.appName
    def deploymentFile = config.deploymentFile ?: "deployment.yaml"
    
    def workDir = "k8s-manifests-${UUID.randomUUID().toString()}"
    
    dir(workDir) {
        checkout([
            $class: 'GitSCM',
            branches: [[name: 'main']],
            userRemoteConfigs: [[
                url: manifestsRepo,
                credentialsId: credentialsId
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
            git push origin main
        """
    }
    
    // Return the directory path for the next stage
    return workDir
}