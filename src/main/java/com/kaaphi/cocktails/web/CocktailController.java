package com.kaaphi.cocktails.web;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class CocktailController {
  private final RecipeData data;

  @Inject
  public CocktailController(RecipeData data) {
    this.data = data;
  }

  public Handler render(String resource) {
    return ctx -> ctx.render(resource);
  }

  public void renderRecipe(Context ctx) {
    String uriTitle = ctx.pathParam("id");
    data.getRecipe(uriTitle).ifPresentOrElse(
        rm -> ctx.render("recipe.html", model(b -> b.put("recipe", rm))),
        () -> ctx.status(404));
  }

  public void renderAlphabeticalIndex(Context ctx) {
    renderRecipeList(ctx, new RecipeListModel("Alphabetical", data.getRecipes(__ -> true)));
  }

  private void renderRecipeList(Context ctx, Map<String, ?> model) {
    ctx.render("recipeList.html", model);
  }

  private static Map<String, Object> model(Consumer<ImmutableMap.Builder<String, Object>> consumer) {
    ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
    consumer.accept(builder);
    return new HashMap<>(builder.build());
  }

  private static class RecipeListModel extends HashMap<String, Object> {
    public static enum Key {
      title,
      recipes
    }

    public RecipeListModel(String title, List<RecipeModel> recipes) {
      put(Key.title.name(), title);
      put(Key.recipes.name(), recipes);
    }
  }
}
