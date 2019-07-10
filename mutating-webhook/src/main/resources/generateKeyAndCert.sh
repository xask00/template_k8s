#!/bin/sh

SERVICENAME_WITHOUT_FQDN="webhook"
NAMESPACE="webhooks"
CERTIFICATE_NAME=$SERVICENAME_WITHOUT_FQDN
KEYSTORE_PASSWORD=changeit
FILE_CSR=server.csr

openssl req -new -nodes -newkey rsa:2048 -keyout server.key -out $FILE_CSR -config <(
cat <<EOF
[req]
default_bits = 2048
prompt = no
default_md = sha256
req_extensions = req_ext
distinguished_name = dn

[ dn ]
C=US
ST=New York
L=Rochester
O=End Point
OU=Testing Domain
emailAddress=your-administrative-address@your-awesome-existing-domain.com
CN = $SERVICENAME_WITHOUT_FQDN

[ req_ext ]
subjectAltName = @alt_names

[ alt_names ]
DNS.1 = $SERVICENAME_WITHOUT_FQDN
DNS.2 = $SERVICENAME_WITHOUT_FQDN.$NAMESPACE
EOF
)


cat <<EOF | kubectl apply -f -
apiVersion: certificates.k8s.io/v1beta1
kind: CertificateSigningRequest
metadata:
  name: $CERTIFICATE_NAME
spec:
  request: $(cat $FILE_CSR | base64 | tr -d '\n')
  usages:
  - digital signature
  - key encipherment
  - server auth
EOF

kubectl get csr $CERTIFICATE_NAME
# TODO: check if csr is created
kubectl certificate approve $CERTIFICATE_NAME
# TODO : if certificate is generated, get certificate
kubectl get csr $CERTIFICATE_NAME  -o jsonpath='{.status.certificate}'|base64 -d > server.crt

# Combine key and certificate in a file
cat server.key  server.crt > both

#Create openssl pkcs12
# Jave can use PKCS12 directly
openssl pkcs12 -export -in both -out keystore.pkcs12 -passout pass:$KEYSTORE_PASSWORD

# Create Java keystore from pcks12
#keytool -importkeystore -deststorepass changeit -destkeypass changeit -destkeystore ssl-keystore.jks -srckeystore keystore.pkcs12 -srcstoretype PKCS12 -srcstorepass "$KEYSTORE_PASSWORD" -alias 1
