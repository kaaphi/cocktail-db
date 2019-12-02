package com.kaaphi.cocktails.web;

import com.kaaphi.cocktails.domain.Recipe;
import java.util.Objects;

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
