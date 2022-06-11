package com.kaaphi.cocktails.domain;

import com.kaaphi.cocktails.util.Fractions;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;


public class RecipeElement implements Serializable {
  private static final long serialVersionUID = 1L;

  private String ingredient;
  private int[] amount;
  private String note;
  private String unit;
  private Boolean isBase;

  public RecipeElement(String ingredient, int[] amount, String unit, String note, Boolean isBase) {
    this.ingredient = ingredient;
    this.amount = amount;
    this.note = note;
    this.unit = unit;
    this.isBase = isBase;
  }

  public RecipeElement() {
    this("", new int[]{0,0}, "oz", "", false);
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

  public Boolean getIsBase() {
    return isBase;
  }

  public void setIsBase(Boolean isBase) {
    this.isBase = isBase;
  }

  public String getUnit() {
    return unit;
  }

  public void setUnit(String unit) {
    this.unit = unit;
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
    result = prime * result + Objects.hash(ingredient, isBase, note, unit);
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
        && Objects.equals(unit, other.unit);
  }


}
