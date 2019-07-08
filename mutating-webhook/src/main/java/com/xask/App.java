package com.xask;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import io.fabric8.kubernetes.api.model.admission.AdmissionRequest;
import io.fabric8.kubernetes.api.model.admission.AdmissionResponse;
import io.fabric8.kubernetes.api.model.admission.AdmissionReview;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

import static spark.Spark.*;

public class App {

    final static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void main(String[] args) {

        Gson gson = new Gson();
        log.info("Starting App");

        // Configuring SSL for spark java
        // This is important since when k8s --calls--> mutating webhook it should have ssl signed by k8s CA
        String keyStoreLocation = "deploy/keystore.jks";
        String keyStorePassword = "password";
        secure(keyStoreLocation, keyStorePassword, null, null);

        port(8080);
        get("/mutate", "application/json", (req, res) -> {
            //V1beta1Admission.AdmissionReview
            res.type("application/json");
            return  ImmutableMap.of("test", ImmutableList.of("1", "2", "3"));
        }, gson::toJson);


        post("/mutate", "application/json", (req, res) -> {
            AdmissionReview admissionReview = gson.fromJson(req.body(), AdmissionReview.class);
            AdmissionRequest admissionRequest = admissionReview.getRequest();
            log.info("HTTP request body = "+req.body());
            log.info("Admission Review Object = "+ admissionReview);
            log.info("Admission Request = "+admissionRequest);
            AdmissionResponse admissionResponse = admissionReview.getResponse();
            log.info("Admission Response = "+admissionResponse);

            res.type("application/json");
            return  ImmutableMap.of("test", ImmutableList.of("1", "2", "3"));
        }, gson::toJson);
    }
}
