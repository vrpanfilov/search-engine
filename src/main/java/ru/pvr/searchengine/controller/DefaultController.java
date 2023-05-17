package ru.pvr.searchengine.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class DefaultController {

  @RequestMapping("/api/v1/")
  public String index() {
    return "index";
  }
}
