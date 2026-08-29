package com.projects.money_controll.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/values")
public class AddedValuesController {



    @GetMapping
    public void teste(){

        return;
    }


}
