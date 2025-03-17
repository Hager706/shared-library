
// def call(String namespace, String kubeTokenCredId) {
//     withCredentials([string(credentialsId: kubeTokenCredId, variable: 'KUBE_TOKEN')]) {
//         sh """

//             kubectl config use-context minikube
//             kubectl config set-context --current --namespace=${namespace}
//             kubectl apply -f k8s-deployment.yaml
//         """
//     }
// }

def call(String namespace, String kubeTokenCredId, String kubernetesApiServer, String deploymentFile) {
    withCredentials([string(credentialsId: kubeTokenCredId, variable: 'KUBE_TOKEN')]) {
        sh """
            # Configure Kubernetes credentials
            kubectl config set-credentials jenkins-user --token=${KUBE_TOKEN}

            # Configure Kubernetes cluster
            kubectl config set-cluster kubernetes --server=${kubernetesApiServer} --insecure-skip-tls-verify=true

            # Create and set a new context
            kubectl config set-context jenkins-context --cluster=kubernetes --user=jenkins-user --namespace=${namespace}
            kubectl config use-context jenkins-context

            # Apply the deployment
            kubectl apply -f ${deploymentFile} --validate=false
        """
    }
}
   