#!/usr/bin/env groovy

def call(Map config) {
    def imageName = config.imageName
    def tag = config.tag ?: 'latest'
    
    sh """
        docker rmi ${imageName}:${tag} || true
    """
}