def call(Map config) {
    def manifestsRepo = config.manifestsRepo
    def credentialsId = config.credentialsId
    def imageName = config.imageName
    def imageTag = config.imageTag
    def appName = config.appName
    def deploymentFile = config.deploymentFile ?: "deployment.yaml"
    
    def workDir = "k8s-manifests-${UUID.randomUUID().toString()}"
    
    dir(workDir) {
        // Checkout using credentials
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
            git checkout -B main origin/main
            sed -i "s|image: ${imageName}:.*|image: ${imageName}:${imageTag}|g" ${deploymentFile}
            grep -n "image: ${imageName}" ${deploymentFile}
            git config user.email "jenkins@example.com"
            git config user.name "Jenkins"
            git add ${deploymentFile}
            git commit -m "Update ${appName} image to ${imageTag}"
        """
        
        // Use withCredentials to properly authenticate the Git push
        withCredentials([usernamePassword(credentialsId: credentialsId, 
                                         usernameVariable: 'GIT_USERNAME', 
                                         passwordVariable: 'GIT_PASSWORD')]) {
            // Extract the repository URL without protocol
            def repoUrl = manifestsRepo.replaceFirst("https://", "")
            
            // Use authenticated URL for push
            sh """
                git push https://${GIT_USERNAME}:${GIT_PASSWORD}@${repoUrl} main
            """
        }
    }
    
    return workDir
}