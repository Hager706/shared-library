def call(String namespace, String kubeTokenCredId) {
    withCredentials([string(credentialsId: kubeTokenCredId, variable: 'KUBE_TOKEN')]) {
        sh """
            kubectl config set-credentials jenkins-user --token=${KUBE_TOKEN}
            kubectl config set-context --current --user=jenkins-user --namespace=${namespace}
            kubectl apply -f k8s-deployment.yaml
        """
    }
}