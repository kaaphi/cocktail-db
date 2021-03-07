package com.kaaphi.cocktails.web;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.kaaphi.cocktails.domain.Recipe;
import com.kaaphi.cocktails.domain.RecipeElement;
import com.kaaphi.cocktails.web.data.RecipeData;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    renderRecipeList(ctx, new RecipeListModel("Alphabetical", data.getRecipes(Predicate.not(Recipe::isArchived))));
  }
  
  public void renderSearch(Context ctx) {
    String searchString = ctx.queryParam("q");
    renderRecipeList(ctx, new RecipeListModel(String.format("\u201C%s\u201D", searchString), 
        data.getRecipes(RecipeData.getSearchPredicate(searchString))));
  }
  
  public void renderByBaseIngredients(Context ctx) {
    renderCategorizedRecipes(ctx, "Base Ingredients", r -> r.getBaseSpirits().stream().map(RecipeElement::getIngredient));
  }
  
  public void renderByAllIngredients(Context ctx) {
    renderCategorizedRecipes(ctx, "All Ingredients", r -> r.getIndexElements() ? 
        r.getRecipeElements().stream().map(RecipeElement::getIngredient)
        : Stream.empty());
  }

  private void renderRecipeList(Context ctx, Map<String, ?> model) {
    ctx.render("recipeList.html", model);
  }
  
  private void renderCategorizedRecipes(Context ctx, String title, Function<Recipe, Stream<String>> classifier) {
    Map<CategoryModel, List<RecipeModel>> categorized = data.getRecipesByCategory(Predicate.not(Recipe::isArchived), classifier);
    
    ctx.render("categorizedRecipeList.html", model(b -> b
        .put("title", title)
        .put("categories", categorized.keySet().stream().sorted().collect(Collectors.toList()))
        .put("categoryMap", categorized)
        ));
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
