#!/usr/bin/env groovy

def call() {
    echo "Running unit tests..."
    
    // For Maven project
    sh 'mvn test'
    
    // Archive test results if needed
    junit '**/target/surefire-reports/*.xml'
    
    echo "Unit tests completed successfully"
}