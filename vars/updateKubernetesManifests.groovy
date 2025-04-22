def call(Map config) {
    def manifestsRepo = config.manifestsRepo
    def credentialsId = config.credentialsId
    def imageName = config.imageName
    def imageTag = config.imageTag
    def appName = config.appName
    def deploymentFile = config.deploymentFile ?: "deployment.yaml"
    
    def workDir = "k8s-manifests-${UUID.randomUUID().toString()}"
    
    dir(workDir) {
        // Checkout using HTTPS credentials
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
        
        // Push using the same credentials used for checkout
        withCredentials([usernamePassword(
            credentialsId: credentialsId,
            passwordVariable: 'GIT_PASSWORD',
            usernameVariable: 'GIT_USERNAME'
        )]) {
            sh """
                git push https://${GIT_USERNAME}:${GIT_PASSWORD}@github.com/Hager706/kubernetes-manifests.git main
            """
        }
    }
    
    return workDir
}