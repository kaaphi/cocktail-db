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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CocktailAppModule extends AbstractModule {
  private static final Logger log = LoggerFactory.getLogger(CocktailApp.class);
  
  private static final String CONFIG_PATH_SYSTEM_PROPERTY = "configPath";
  
  @Override
  protected void configure() {
    Names.bindProperties(binder(), loadProperties("config.properties"));

    bindConstant()
    .annotatedWith(Names.named("port"))
    .to(7000);

  }

  static Properties loadProperties(String resource) {
    try(InputStream in = getPropertiesInputStream(resource)) {
      Properties props = new Properties();
      if(in != null) {
        props.load(in);
        log.info("Loaded config properties from {}.", resource);
      }
      return props;
    } catch (IOException e) {
      throw new Error(e);
    }
  }
  
  private static InputStream getPropertiesInputStream(String name) throws IOException {
    String configPathString = System.getProperty(CONFIG_PATH_SYSTEM_PROPERTY);
    if(configPathString != null) {
      Path configFilePath = Paths.get(configPathString).resolve(name);
      log.info("Attempting to load {} from {}.", name, configFilePath);
      if(Files.exists(configFilePath)) {
        return Files.newInputStream(configFilePath);
      } else {
        log.warn("Config file {} does not exist!", configFilePath);
      }
    }
    
    log.info("Attempting to load {} from classpath.", name);
    InputStream is = CocktailAppModule.class.getClassLoader().getResourceAsStream(name);
    if(is == null) {
      log.warn("Could not load {} from classpath!", name);
    }
    
    return is;
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
