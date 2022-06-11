package com.kaaphi.cocktails.web;

import com.kaaphi.cocktails.domain.Recipe;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class RecipeModel implements Comparable<RecipeModel> {
  private final String uriTitle;
  private final Recipe recipe;

  public RecipeModel(String uriTitle, Recipe recipe) {
    this.uriTitle = uriTitle;
    this.recipe = recipe;
  }

  public String getUriTitle() {
    return uriTitle;
  }

  public Recipe getRecipe() {
    return recipe;
  }
  
  public boolean hasNote() {
    return !(recipe.getNote() == null || recipe.getNote().trim().isEmpty());
  }
  
  public List<RecipeElementModel> getElements() {
    return recipe.getRecipeElements().stream()
        .map(RecipeElementModel::new)
        .collect(Collectors.toList());
  }

  @Override
  public int compareTo(RecipeModel o) {
    return this.recipe.getName().compareTo(o.recipe.getName());
  }

  @Override
  public int hashCode() {
    return Objects.hash(recipe);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof RecipeModel)) {
      return false;
    }
    RecipeModel other = (RecipeModel) obj;
    return Objects.equals(recipe, other.recipe);
  }
}
