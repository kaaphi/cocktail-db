package com.kaaphi.cocktails.web;

import org.apache.velocity.app.VelocityEngine;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import com.google.inject.util.Modules;

public class RunDevCocktailDb {
  public static void main(String[] args) throws Exception {
    Injector injector = Guice.createInjector(
        Modules.override(
            new CocktailAppModule(),
            new DevVelocityModule()
            )
        .with(
            new OverrideProps()
            )
        );
    
    CocktailApp app = injector.getInstance(CocktailApp.class);
    
    app.start();
  }
  
  public static class OverrideProps extends AbstractModule {
    @Override
    protected void configure() {
      Names.bindProperties(binder(), CocktailAppModule.loadProperties("config-test.properties"));
    }
  }
  
  public static class DevVelocityModule extends VelocityModule {
    @Override
    protected void configureVelocityEngine(VelocityEngine velocityEngine) {
      velocityEngine.setProperty("resource.loader", "file");  
      velocityEngine.setProperty("velocimacro.library.autoreload", "true");
      velocityEngine.setProperty("file.resource.loader.cache", "false");
      velocityEngine.setProperty("file.resource.loader.path", "./src/main/resources");
    }
  }
}
