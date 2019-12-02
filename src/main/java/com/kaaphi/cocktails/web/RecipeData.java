package com.kaaphi.cocktails.web;

import com.google.inject.Inject;
import com.kaaphi.cocktails.dao.RecipeDao;
import com.kaaphi.cocktails.domain.Recipe;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RecipeData {
  private final Map<String, RecipeModel> recipes;

  @Inject
  public RecipeData(RecipeDao dao) throws Exception {
    List<Recipe> rawRecipes = dao.load();
    recipes = new HashMap<>();

    for(Recipe r : rawRecipes) {
      String rootUriTitle = generateUriName(r.getName());;
      String uriTitle = rootUriTitle;
      for(int i = 2; recipes.containsKey(uriTitle); i++) {
        uriTitle = rootUriTitle + "-" + i;
      }

      recipes.put(uriTitle, new RecipeModel(uriTitle, r));
    }
  }

  public Optional<RecipeModel> getRecipe(String uriTitle) {
    return Optional.ofNullable(recipes.get(uriTitle));
  }
  
  public Map<CategoryModel, List<RecipeModel>> getRecipesByCategory(Function<Recipe, Stream<String>> categorizer) {
    Map<CategoryModel, List<RecipeModel>> categorized = new HashMap<CategoryModel, List<RecipeModel>>();
    
    Function<RecipeModel, Stream<CategoryModel>> classifier = categorizer
        .compose(RecipeModel::getRecipe)
        .andThen(s -> s.map(c -> new CategoryModel(c, generateUriName(c))));
        
    
    recipes.values().stream()
    .sorted()
    .forEach(rm -> {
      classifier.apply(rm).forEach(c -> {
        categorized.computeIfAbsent(c, __ -> new LinkedList<>()).add(rm);
      });
    });
    
    return categorized;
  }

  public List<RecipeModel> getRecipes(Predicate<Recipe> filter) {
    return recipes.values().stream()
        .filter(rm -> filter.test(rm.getRecipe()))
        .sorted()
        .collect(Collectors.toList());
  }

  private static final String generateUriName(String title) {
    return title.toLowerCase().replaceAll("\\s+", "-").replaceAll("[^-\\w]", "");
  }
  
  
}
