package com.kaaphi.cocktails.web.data;

import com.google.inject.Inject;
import com.kaaphi.cocktails.dao.RecipeDao;
import com.kaaphi.cocktails.domain.Recipe;
import com.kaaphi.cocktails.domain.RecipeElement;
import com.kaaphi.cocktails.web.CategoryModel;
import com.kaaphi.cocktails.web.RecipeModel;
import com.kaaphi.cocktails.web.data.RecipeDataWatcher.RecipeDataWatcherListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecipeData {
  private static final Logger log = LoggerFactory.getLogger(RecipeData.class);

  private final Map<String, RecipeModel> recipes;
  private final RecipeDao dao;

  @Inject
  public RecipeData(RecipeDao dao, RecipeDataWatcher watcher) throws Exception {
    this.dao = dao;
    recipes = new HashMap<>();
    reloadData();

    watcher.addListener(this::notifiedDataChanged);
  }

  private void notifiedDataChanged() {
    try {
      log.info("Data changed, will reload.");
      reloadData();
    } catch (Exception e) {
      log.error("Failed to reload data!", e);
    }
  }

  public void reloadData() throws Exception {
    List<Recipe> rawRecipes = dao.load();
    recipes.clear();

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
  
  public Map<CategoryModel, List<RecipeModel>> getRecipesByCategory(Predicate<Recipe> filter, Function<Recipe, Stream<String>> categorizer) {
    Map<CategoryModel, List<RecipeModel>> categorized = new HashMap<CategoryModel, List<RecipeModel>>();
    
    Function<RecipeModel, Stream<CategoryModel>> classifier = categorizer
        .compose(RecipeModel::getRecipe)
        .andThen(s -> s.map(c -> new CategoryModel(c, generateUriName(c))));
        
    
    recipes.values().stream()
    .filter(rm -> filter.test(rm.getRecipe()))
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
  
  public static Predicate<Recipe> getSearchPredicate(String searchString) {
    final String containString = searchString.toLowerCase();
    return r -> r.getName().toLowerCase().contains(containString)
        || r.getRecipeElements().stream()
          .map(RecipeElement::getIngredient)
          .map(String::toLowerCase)
          .anyMatch(s -> s.contains(containString));
  }

  private static final String generateUriName(String title) {
    return title.toLowerCase().replaceAll("\\s+", "-").replaceAll("[^-\\w]", "");
  }
  
  
}
