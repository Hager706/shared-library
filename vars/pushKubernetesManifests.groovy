#!/usr/bin/env groovy

def call(Map config) {
    def workDir = config.workDir
    
    if (workDir == null || workDir.trim() == "") {
        error "workDir parameter must be specified for pushManifests"
        return
    }
    
    echo "Pushing changes from directory: ${workDir}"
    
    dir(workDir) {
        withCredentials([usernamePassword(credentialsId: config.credentialsId, 
                                         passwordVariable: 'GIT_PASSWORD', 
                                         usernameVariable: 'GIT_USERNAME')]) {
            sh """
                git commit -m "Update image tag for build #${env.BUILD_NUMBER}" || echo "No changes to commit"
                
                git config --local credential.helper '!f() { echo "username=${GIT_USERNAME}"; echo "password=${GIT_PASSWORD}"; }; f'
                
                git push origin main || echo "Push failed - possibly no changes to push"
            """
        }
    }
}