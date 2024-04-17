package ru.kirill.chess.rest;


import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class RestVK {

    private final String SERVER_HOST = "api.vk.com";
    private final String SERVER_URL = "http://" + SERVER_HOST + "/method/";
    private final String a = "https://api.vk.com/method/account.getProfileInfo?access_token=";
    private final String b = "&v=5.131";

    private RestTemplate restTemplate;

    public RestVK() {
        this.restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
    }

    public String getUserId(String token) {
        try {
            Map map = restTemplate.getForObject(a + token + b, Map.class, token);
            Map res = (Map) map.get("response");
            if(res == null){
                return null;
            }
            Object id = res.get("id");
            return id != null ? id.toString() : null;
        } catch (Exception e) {
            throw new RuntimeException("Error while getting id", e);
        }
    }
}