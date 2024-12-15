apiVersion: jenkins.io/v1alpha2
kind: Jenkins
metadata:
  name: master
  namespace: default
spec:
  configurationAsCode:
    configurations: []
    secret:
      name: ""
  groovyScripts:
    configurations: []
    secret:
      name: ""
  jenkinsAPISettings:
    authorizationStrategy: createUser
  master:
    disableCSRFProtection: false
    containers:
      - name: jenkins-master
        image: jenkins/jenkins:lts
        imagePullPolicy: Always
        livenessProbe:
          failureThreshold: 12
          httpGet:
            path: /login
            port: http
            scheme: HTTP
          initialDelaySeconds: 100
          periodSeconds: 10
          successThreshold: 1
          timeoutSeconds: 5
        readinessProbe:
          failureThreshold: 10
          httpGet:
            path: /login
            port: http
            scheme: HTTP
          initialDelaySeconds: 80
          periodSeconds: 10
          successThreshold: 1
          timeoutSeconds: 1
        resources:
          requests:
            cpu: 250m
            memory: 256Mi
          limits:
            cpu: 1500m
            memory: 1Gi
    basePlugins:
      - name: kubernetes
        version: "4302.va_756e4b_67715"
      - name: workflow-job
        version: "1472.ve4d5eca_143c4"
      - name: workflow-aggregator
        version: "600.vb_57cdd26fdd7"
      - name: git
        version: "5.6.0"
      - name: job-dsl
        version: "1.90"
      - name: configuration-as-code
        version: "1903.v004d55388f30"
      - name: kubernetes-credentials-provider
        version: "1.262.v2670ef7ea_0c5"
  service:
    type: LoadBalancer
    port: 80
    loadBalancerSourceRanges: ["${MY_PUBLIC_IP}/32"]
