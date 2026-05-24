package it.uniroma3.siw.torneo_calcio.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String getHome(){
        return "index.html";
    }
}

