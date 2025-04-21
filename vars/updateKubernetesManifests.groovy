def call(Map config) {
    // Make credentialsId optional since the repo is public
    def manifestsRepo = config.manifestsRepo
    def imageName = config.imageName
    def imageTag = config.imageTag
    def appName = config.appName
    def deploymentFile = config.deploymentFile ?: "deployment.yaml"
    def credentialsId = config.get('credentialsId', null) // Safely get credentialsId or null
    
    def workDir = "k8s-manifests-${UUID.randomUUID().toString()}"
    
    dir(workDir) {
        // Configure checkout based on whether credentials are provided
        def checkoutConfig = [
            $class: 'GitSCM',
            branches: [[name: 'main']],
            extensions: [[$class: 'CleanBeforeCheckout']],
            userRemoteConfigs: [[url: manifestsRepo]]
        ]
        
        if (credentialsId) {
            checkoutConfig.userRemoteConfigs[0].credentialsId = credentialsId
        }
        
        checkout(checkoutConfig)
        
        sh """
            git checkout -B main origin/main
            sed -i "s|image: ${imageName}:.*|image: ${imageName}:${imageTag}|g" ${deploymentFile}
            grep -n "image: ${imageName}" ${deploymentFile}
            git config user.email "jenkins@ivolve.com"
            git config user.name "Jenkins Pipeline"
            git add ${deploymentFile}
            git commit -m "Update ${appName} image to ${imageTag}"
            
            ${credentialsId ? """
                git remote set-url origin https://${credentialsId}@github.com/Hager706/kubernetes-manifests.git
                git push origin main
            """ : """
                echo "Repository is public - changes were made but not pushed"
                echo "You can manually push changes from: ${env.WORKSPACE}/${workDir}"
            """}
        """
    }
    
    return workDir
}