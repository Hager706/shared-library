
// def call(String namespace, String kubeTokenCredId) {
//     withCredentials([string(credentialsId: kubeTokenCredId, variable: 'KUBE_TOKEN')]) {
//         sh """

//             kubectl config use-context minikube
//             kubectl config set-context --current --namespace=${namespace}
//             kubectl apply -f k8s-deployment.yaml
//         """
//     }
// }
   
//    def call(String namespace, String kubeTokenCredId) {
//     withCredentials([string(credentialsId: kubeTokenCredId, variable: 'KUBE_TOKEN')]) {
//         sh """
//             # Configure kubectl auth
//             kubectl config set-credentials jenkins --token=${KUBE_TOKEN}
//             kubectl config set-context minikube --user=jenkins
            
//             kubectl config use-context minikube
//             kubectl config set-context --current --namespace=${namespace}
            
//             # Replace the image with the correct tag
//             sed -i 's|hagert/multi-app|hagert/multi-app:${namespace}|g' k8s-deployment.yaml
            
//             kubectl apply -f k8s-deployment.yaml
            
//             # Restore the original file
//             git checkout -- k8s-deployment.yaml || true
//         """
//     }
// }
def call(String namespace, String kubeTokenCredId) {
    withCredentials([string(credentialsId: kubeTokenCredId, variable: 'KUBE_TOKEN')]) {
        // Use a more secure way to pass the token
        sh """
            # Configure kubectl auth
            kubectl config set-credentials jenkins --token=\${KUBE_TOKEN}
            kubectl config set-context minikube --user=jenkins
            
            kubectl config use-context minikube
            kubectl config set-context --current --namespace=${namespace}
            
            # Create a temporary file with the correct image tag
            cat k8s-deployment.yaml | sed "s|hagert/multi-app|hagert/multi-app:${namespace}|g" > temp-deployment.yaml
            
            # Apply the temporary file
            kubectl apply -f temp-deployment.yaml
            
            # Clean up
            rm temp-deployment.yaml
        """
    }
}