def call(String namespace, String kubeTokenCredId) {
    withCredentials([string(credentialsId: kubeTokenCredId, variable: 'KUBE_TOKEN')]) {
        sh """
            # Create a kubeconfig file with the correct local port
            cat > kubeconfig-${namespace} << EOF
            apiVersion: v1
            kind: Config
            clusters:
            - name: minikube
              cluster:
                server: https://127.0.0.1:58815
                insecure-skip-tls-verify: true
            users:
            - name: jenkins-user
              user:
                token: ${KUBE_TOKEN}
            contexts:
            - name: jenkins-context
              context:
                cluster: minikube
                user: jenkins-user
                namespace: ${namespace}
            current-context: jenkins-context
            EOF
            
            # Apply the deployment using the correct port
            kubectl --kubeconfig=kubeconfig-${namespace} apply -f k8s-deployment.yaml
        """
    }
}