package com.kaaphi.cocktails.web;

import static io.javalin.apibuilder.ApiBuilder.get;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CocktailApp {
  private static final Logger log = LoggerFactory.getLogger(CocktailApp.class);

  private final Javalin app;
  private final int port;

  @Inject
  public CocktailApp(Javalin app, @Named("port") int port, CocktailController controller) {
    this.app = app;
    this.port = port;

    app.routes(() -> {
      get("/", controller.render("index.html"));
      get("alphabetical", controller::renderAlphabeticalIndex);

      get("/r/:id", controller::renderRecipe);
    });
  }

  public void start() {
    app.start(port);   
    log.info("App started.");
  }

  public static void main(String[] args) {
  }
}
