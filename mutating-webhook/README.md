## How to use this
### Onetime
1. Create secrets 
   kubectl -n <namespace> create secret generic $webHookName-certs --from-file=../keystore.pkcs12
2. kubectl apply -f build/k8s/webHookConfig.yml
3. get CA bundle : `kubectl config view --raw --minify --flatten -o jsonpath='{.clusters[].cluster.certificate-authority-data}'`

### Developing
1. write code
2. `./gradlew clean distTar`
3. Create and push docker image 
   docker build . -t <tag>
4. kubectl apply -f build/k8s/mutatingWebHookDeployment.yml


## Details
The mutating webhook received this object (admission review) from k8s

   https://godoc.org/k8s.io/api/admission/v1beta1#AdmissionReview

and after processing it return this object back to k8s.

The object internally has AdmissionReview (https://godoc.org/k8s.io/api/admission/v1beta1#AdmissionRequest)
and AdmissionResponse (https://godoc.org/k8s.io/api/admission/v1beta1#AdmissionResponse)

```
type AdmissionRequest struct {
    UID types.UID `json:"uid" protobuf:"bytes,1,opt,name=uid"`

    // Kind is the fully-qualified type of object being submitted (for example, v1.Pod or autoscaling.v1.Scale)
    Kind metav1.GroupVersionKind `json:"kind" protobuf:"bytes,2,opt,name=kind"`

    // Resource is the fully-qualified resource being requested (for example, v1.pods)
    Resource metav1.GroupVersionResource `json:"resource" protobuf:"bytes,3,opt,name=resource"`

    // SubResource is the subresource being requested, if any (for example, "status" or "scale"), +optional
    SubResource string `json:"subResource,omitempty" protobuf:"bytes,4,opt,name=subResource"`

    // RequestKind is the fully-qualified type of the original API request (for example, v1.Pod or autoscaling.v1.Scale).
    RequestKind *metav1.GroupVersionKind `json:"requestKind,omitempty" protobuf:"bytes,13,opt,name=requestKind"`

    // RequestResource is the fully-qualified resource of the original API request (for example, v1.pods).
    RequestResource *metav1.GroupVersionResource `json:"requestResource,omitempty" protobuf:"bytes,14,opt,name=requestResource"`

    // RequestSubResource is the name of the subresource of the original API request, if any (for example, "status" or "scale")
    RequestSubResource string `json:"requestSubResource,omitempty" protobuf:"bytes,15,opt,name=requestSubResource"`

    // Name is the name of the object as presented in the request.  On a CREATE operation, the client may omit name and
    Name string `json:"name,omitempty" protobuf:"bytes,5,opt,name=name"`

    // Namespace is the namespace associated with the request (if any).    // +optional
    Namespace string `json:"namespace,omitempty" protobuf:"bytes,6,opt,name=namespace"`

    // Operation is the operation being performed. This may be different than the operation
    // requested. e.g. a patch can result in either a CREATE or UPDATE Operation.
    Operation Operation `json:"operation" protobuf:"bytes,7,opt,name=operation"`
    // UserInfo is information about the requesting user
    UserInfo authenticationv1.UserInfo `json:"userInfo" protobuf:"bytes,8,opt,name=userInfo"`
    // Object is the object from the incoming request prior to default values being applied
    // +optional
    Object runtime.RawExtension `json:"object,omitempty" protobuf:"bytes,9,opt,name=object"`
    // OldObject is the existing object. Only populated for UPDATE requests.
    // +optional
    OldObject runtime.RawExtension `json:"oldObject,omitempty" protobuf:"bytes,10,opt,name=oldObject"`
    // DryRun indicates that modifications will definitely not be persisted for this request.
    // Defaults to false.
    // +optional
    DryRun *bool `json:"dryRun,omitempty" protobuf:"varint,11,opt,name=dryRun"`
    // Options is the operation option structure of the operation being performed.
    // e.g. `meta.k8s.io/v1.DeleteOptions` or `meta.k8s.io/v1.CreateOptions`. This may be
    // different than the options the caller provided. e.g. for a patch request the performed
    // Operation might be a CREATE, in which case the Options will a
    // `meta.k8s.io/v1.CreateOptions` even though the caller provided `meta.k8s.io/v1.PatchOptions`.
    // +optional
    Options runtime.RawExtension `json:"options,omitempty" protobuf:"bytes,12,opt,name=options"`
}
```

openssl req -nodes -newkey rsa:2048 -keyout server.key -out server.csr -subj "/C=GB/ST=London/L=London/O=Global Security/OU=IT Department/CN=example.com"

cat <<EOF | kubectl apply -f -
apiVersion: certificates.k8s.io/v1beta1
kind: CertificateSigningRequest
metadata:
  name: my-svc.my-namespace
spec:
  request: $(cat server.csr | base64 | tr -d '\n')
  usages:
  - digital signature
  - key encipherment
  - server auth
EOF

kubectl certificate approve my-svc.my-namespace
kubectl get csr my-svc.my-namespace  -o jsonpath='{.status.certificate}'

cat server.key  server.crt > both

openssl pkcs12 -export -in both -out keystore.pkcs12 -passout pass:changeit

// creating java keystore
keytool -importkeystore -deststorepass changeit -destkeypass changeit -destkeystore my-keystore.jks -srckeystore keystore.pkcs12 -srcstoretype PKCS12 -srcstorepass changeit -alias 1


//create secret and use it in spark java


## References


https://www.wowza.com/docs/how-to-import-an-existing-ssl-certificate-and-private-key#convert-the-certificate-and-private-key-to-pkcs-12

http://cunning.sharp.fm/2008/06/importing_private_keys_into_a.html
https://web.archive.org/web/20180924184818/http://cunning.sharp.fm/2008/06/importing_private_keys_into_a.html


