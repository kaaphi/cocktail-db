package com.kaaphi.cocktails.domain;

import com.kaaphi.cocktails.util.Fractions;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;


public class RecipeElement implements Serializable {
  private static final long serialVersionUID = 1L;

  private String ingredient;
  private int[] amount;
  private Recipe recipe;
  private String note;
  private String unit;
  private boolean isBase;

  public RecipeElement(String ingredient, int[] amount, String unit, String note, boolean isBase, Recipe recipe) {
    this.ingredient = ingredient;
    this.amount = amount;
    this.note = note;
    this.recipe = recipe;
    this.unit = unit;
    this.isBase = isBase;
  }

  public RecipeElement(Recipe recipe) {
    this("", new int[]{0,0}, "oz", "", false, recipe);
  }

  public String getIngredient() {
    return ingredient;
  }

  public void setIngredient(String ingredient) {
    this.ingredient = ingredient;
  }

  public int[] getAmount() {
    return amount;
  }

  public void setAmount(int[] amount) {
    this.amount = amount;
  }

  public boolean isBase() {
    return isBase;
  }

  public void setIsBase(boolean isBase) {
    this.isBase = isBase;
  }

  public String getUnit() {
    return unit;
  }

  public void setUnit(String unit) {
    this.unit = unit;
  }

  public Recipe getRecipe() {
    return recipe;
  }

  public void setRecipe(Recipe recipe) {
    this.recipe = recipe;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }

  @Override
  public String toString() {
    return String.format("%s %s %s (%b)", Fractions.toString(amount), unit, ingredient, isBase);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(amount);
    result = prime * result + Objects.hash(ingredient, isBase, note, recipe, unit);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof RecipeElement)) {
      return false;
    }
    RecipeElement other = (RecipeElement) obj;
    return Arrays.equals(amount, other.amount) && Objects.equals(ingredient, other.ingredient)
        && isBase == other.isBase && Objects.equals(note, other.note)
        && Objects.equals(recipe, other.recipe) && Objects.equals(unit, other.unit);
  }


}
