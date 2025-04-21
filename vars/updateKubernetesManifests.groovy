// In your shared library (vars/updateKubernetesManifests.groovy)
def call(Map config) {
    // Required parameters
    def manifestsRepo = config.manifestsRepo
    def imageName = config.imageName
    def imageTag = config.imageTag
    def appName = config.appName
    
    // Optional parameters with defaults
    def deploymentFile = config.deploymentFile ?: "deployment.yaml"
    def credentialsId = config.get('credentialsId', null)
    
    def workDir = "k8s-manifests-${UUID.randomUUID().toString()}"
    
    dir(workDir) {
        // Configure checkout
        def checkoutConfig = [
            $class: 'GitSCM',
            branches: [[name: 'main']],
            extensions: [[$class: 'CleanBeforeCheckout']],
            userRemoteConfigs: [[url: manifestsRepo]]
        ]
        
        // Add credentials if provided
        if (credentialsId) {
            checkoutConfig.userRemoteConfigs[0].credentialsId = credentialsId
        }
        
        checkout(checkoutConfig)
        
        sh """
            git checkout -B main origin/main
            sed -i "s|image: ${imageName}:.*|image: ${imageName}:${imageTag}|g" ${deploymentFile}
            grep -n "image: ${imageName}" ${deploymentFile}
            git config user.email "jenkins@example.com"
            git config user.name "Jenkins"
            git add ${deploymentFile}
            git commit -m "Update ${appName} image to ${imageTag}"
            
            ${credentialsId ? """
                git push origin main
            """ : """
                echo "Warning: No credentials provided - skipping push"
                echo "Modified files are in: ${env.WORKSPACE}/${workDir}"
            """}
        """
    }
    
    return workDir
}