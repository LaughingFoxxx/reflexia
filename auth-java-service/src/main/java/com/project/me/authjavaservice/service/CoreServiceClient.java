package com.project.me.authjavaservice.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "central-java-service", url = "http://localhost:8081")
public interface CoreServiceClient {

    @PostMapping("/text/create-new-user")
    void createNewUser(@RequestParam String userEmail);

}
