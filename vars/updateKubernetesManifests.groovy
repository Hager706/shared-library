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
            ]],
            extensions: [
                [$class: 'CloneOption', shallow: false, noTags: false, depth: 0, timeout: 30],
                [$class: 'LocalBranch', localBranch: 'main']  // هذا يضمن أنك على الفرع الرئيسي
            ]
        ])
        
        sh """
            sed -i "s|image: ${imageName}:.*|image: ${imageName}:${imageTag}|g" ${deploymentFile}
            
            grep -n "image: ${imageName}" ${deploymentFile} || echo "Warning: Image pattern not found"
            
            git config user.email "jenkins@ivolve.com"
            git config user.name "Jenkins Pipeline"
            
            git add ${deploymentFile}
        """
    }
    
    return workDir
}