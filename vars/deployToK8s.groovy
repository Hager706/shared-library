
def call(String namespace, String kubeTokenCredId) {
    withCredentials([string(credentialsId: kubeTokenCredId, variable: 'KUBE_TOKEN')]) {
        sh """
            kubectl config use-context minikube
            kubectl config set-context --current --namespace=${namespace}
            cat k8s-deployment.yaml | sed "s|hagert/multi-app|hagert/multi-app:${namespace}|g" > temp-deployment.yaml
            kubectl apply -f temp-deployment.yaml
            rm temp-deployment.yaml
        """
    }
}
   