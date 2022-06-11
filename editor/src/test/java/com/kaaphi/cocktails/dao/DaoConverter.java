package com.kaaphi.cocktails.dao;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import com.kaaphi.cocktails.domain.Recipe;
import com.kaaphi.cocktails.web.MongoRecipeDaoModule;
import java.io.File;
import java.util.List;

public class DaoConverter {
  public static void convert(RecipeDao from, RecipeDao to) throws Exception {
    List<Recipe> recipes = from.load();
    to.save(recipes);
  }

  public static void main(String[] args) throws Exception {
    Injector injector = Guice.createInjector(
        new MongoRecipeDaoModule(),
        binder -> binder.bind(String.class)
            .annotatedWith(Names.named("mongo.connectionString")).toInstance("")
    );
    RecipeDao to = injector.getInstance(RecipeDao.class);

    RecipeDao from = new CustomFormatRecipeDao(new File("db.dat"));

    convert(from, to);
  }
}
