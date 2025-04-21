#!/usr/bin/env groovy

def call(Map config = [:]) {
    echo "Running SonarQube analysis..."
    
    def sonarServer = config.sonarQubeServer ?: 'SonarQube'
    def projectKey = config.projectKey ?: 'default-project'
    def projectName = config.projectName ?: 'Default Project'
    
    withSonarQubeEnv(sonarServer) {
        // For Maven project
        sh """
            mvn sonar:sonar \
                -Dsonar.projectKey=${projectKey} \
                -Dsonar.projectName='${projectName}' \
                -Dsonar.java.binaries=target/classes \
                -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
        """
    }
    
    // Optional: Wait for quality gate
    timeout(time: 1, unit: 'HOURS') {
        def qg = waitForQualityGate()
        if (qg.status != 'OK') {
            echo "SonarQube Quality Gate failed: ${qg.status}"
            // Uncomment if you want to fail the build on quality gate failure
            // error "SonarQube Quality Gate failed: ${qg.status}"
        }
    }
    
    echo "SonarQube analysis completed"
}