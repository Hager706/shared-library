
def call(String namespace, String kubeTokenCredId) {
    withCredentials([string(credentialsId: kubeTokenCredId, variable: 'KUBE_TOKEN')]) {
        sh """

            kubectl config use-context minikube
            kubectl config set-context --current --namespace=${namespace}
            kubectl apply -f k8s-deployment.yaml
        """
    }
}
   