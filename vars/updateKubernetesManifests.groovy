#!/usr/bin/env groovy

def call(Map config) {
    def manifestsRepo = config.manifestsRepo
    def imageName = config.imageName
    def imageTag = config.imageTag
    def appName = config.appName
    def deploymentFile = config.deploymentFile ?: "deployment.yaml"
    
    def workDir = "k8s-manifests-${UUID.randomUUID().toString()}"
    
    dir(workDir) {
        // Clone the public repository (no credentials needed)
        checkout([
            $class: 'GitSCM',
            branches: [[name: 'main']],
            userRemoteConfigs: [[
                url: manifestsRepo
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
            git config user.email "jenkins@ivolve.com"
            git config user.name "Jenkins Pipeline"
            
            # Commit changes
            git add ${deploymentFile}
            git commit -m "Update ${appName} image to ${imageTag}"
            
            # Push changes (only if credentials are provided)
            ${config.credentialsId ? """
                git remote set-url origin https://${config.credentialsId}@github.com/Hager706/kubernetes-manifests.git
                git push origin main
            """ : """
                echo "Warning: No credentials provided - skipping push to repository"
                echo "You can manually push changes from: ${env.WORKSPACE}/${workDir}"
            """}
        """
    }
    
    return workDir
}