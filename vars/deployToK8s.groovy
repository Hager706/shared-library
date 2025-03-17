def call(String namespace, String kubeTokenCredId, String kubernetesApiServer, String deploymentFile) {
    withCredentials([string(credentialsId: kubeTokenCredId, variable: 'KUBE_TOKEN')]) {
        sh """
            # Debugging: Print Kubernetes API server address
            echo "Kubernetes API Server: ${kubernetesApiServer}"

            # Configure Kubernetes credentials
            kubectl config set-credentials jenkins-user --token=${KUBE_TOKEN}

            # Configure Kubernetes cluster
            kubectl config set-cluster kubernetes --server=${kubernetesApiServer} --insecure-skip-tls-verify=true

            # Create and set a new context
            kubectl config set-context jenkins-context --cluster=kubernetes --user=jenkins-user --namespace=${namespace}
            kubectl config use-context jenkins-context

            # Debugging: Verify connectivity to the Kubernetes API server
            echo "Testing connectivity to the Kubernetes API server..."
            curl -k ${kubernetesApiServer}

            # Apply the deployment
            echo "Applying deployment file: ${deploymentFile}"
            kubectl apply -f ${deploymentFile} --validate=false
        """
    }
}