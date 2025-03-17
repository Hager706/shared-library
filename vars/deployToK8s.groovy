
def call(String namespace, String kubeTokenCredId) {
    withCredentials([string(credentialsId: kubeTokenCredId, variable: 'KUBE_TOKEN')]) {
        sh """
            # No need to create a new config, just use the existing one
            # and switch to the right namespace
            kubectl config use-context minikube
            kubectl config set-context --current --namespace=${namespace}
            kubectl apply -f k8s-deployment.yaml
        """
    }
}