#!/usr/bin/env groovy

def call(Map config) {
    def manifestsRepo = config.manifestsRepo
    def credentialsId = config.credentialsId
    def imageName = config.imageName
    def imageTag = config.imageTag
    def appName = config.appName
    def deploymentFile = config.deploymentFile ?: "deployment.yaml"
    
    // Generate a unique directory name
    def workDir = "k8s-manifests-${UUID.randomUUID().toString()}"
    
    dir(workDir) {
        // Use HTTPS URL format if SSH is causing problems
        def repoUrl = manifestsRepo
        if (manifestsRepo.startsWith("git@")) {
            echo "Converting SSH URL to HTTPS format"
            repoUrl = manifestsRepo.replace("git@github.com:", "https://github.com/")
        }
        
        checkout([
            $class: 'GitSCM',
            branches: [[name: 'main']],
            userRemoteConfigs: [[
                url: repoUrl,
                credentialsId: credentialsId
            ]],
            extensions: [[$class: 'CloneOption', shallow: false, noTags: false, depth: 0, timeout: 30]]
        ])
        
        sh """
            # Update the image tag in the deployment file
            sed -i "s|image: ${imageName}:.*|image: ${imageName}:${imageTag}|g" ${deploymentFile}
            
            # Verify the change
            grep -n "image: ${imageName}" ${deploymentFile} || echo "Warning: Image pattern not found"
            
            # Configure Git user
            git config user.email "jenkins@ivolve.com"
            git config user.name "Jenkins Pipeline"
            
            # Stage the changes
            git add ${deploymentFile}
        """
    }
    
    // Return the directory path for the next stage
    return workDir
}