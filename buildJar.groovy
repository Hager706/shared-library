#!/usr/bin/env groovy

def call() {
    echo "Building JAR file..."
    
    // For Maven project
    sh 'mvn clean package -DskipTests'
    
    // Archive the generated JAR file
    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
    
    echo "JAR file built successfully"
}