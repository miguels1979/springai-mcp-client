package com.miguel.course.springai.apps.mcpclient.controllers;

import com.miguel.course.springai.apps.mcpclient.services.WeatherAiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WeatherController {

    private final WeatherAiService service;


    public WeatherController(WeatherAiService service) {
        this.service = service;
    }

    @GetMapping("/api/weather/ask")
    public String askWeather (@RequestParam String question){
        return service.ask(question);
    }

}
