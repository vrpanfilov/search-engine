package ru.pvr.searchengine.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/v1/search-engine")
public class DefaultController {

  @GetMapping
  public String index() {
    return "index";
  }

  @GetMapping("/")
  public String indexSlash() {
    return "index";
  }
}
