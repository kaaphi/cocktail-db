package com.kaaphi.cocktails.web;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.kaaphi.cocktails.dao.CustomFormatRecipeDao;
import com.kaaphi.cocktails.dao.RecipeDao;
import io.javalin.Javalin;
import io.javalin.plugin.rendering.JavalinRenderer;
import io.javalin.plugin.rendering.template.JavalinVelocity;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Properties;
import org.apache.velocity.app.VelocityEngine;

public class CocktailAppModule extends AbstractModule {
  @Override
  protected void configure() {
    Names.bindProperties(binder(), loadProperties("config.properties"));

    bindConstant()
    .annotatedWith(Names.named("port"))
    .to(7000);

  }

  static Properties loadProperties(String resource) {
    try(InputStream in = CocktailAppModule.class.getClassLoader().getResourceAsStream(resource)) {
      Properties props = new Properties();
      if(in != null) {
        props.load(in);
      }
      return props;
    } catch (IOException e) {
      throw new Error(e);
    }
  }

  @Provides
  RecipeDao provideRecipeDao(@Named("recipe.data.path") String dataPath) {
    return new CustomFormatRecipeDao(Paths.get(dataPath).toFile());
  }

  @Provides
  Javalin provideJavalin(VelocityEngine engine) {
    JavalinRenderer.register(JavalinVelocity.INSTANCE, ".html");
    JavalinVelocity.configure(engine);
    return Javalin.create(config -> {
      config.addStaticFiles("/static");
    });
  }
}
