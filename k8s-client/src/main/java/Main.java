import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.models.V1PodList;
import io.kubernetes.client.util.Config;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {

        ApiClient client = null;
        CoreV1Api coreApi = null;
        try {
            client = Config.defaultClient();
            Configuration.setDefaultApiClient(client);
            coreApi = new CoreV1Api();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            V1PodList pods = coreApi.listNamespacedPod("default", false, "false", null, null, null, null, null, null, false);
            for(V1Pod pod: pods.getItems()) {
                System.out.println(pod);
            }
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }
}
