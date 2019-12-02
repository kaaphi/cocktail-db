package com.kaaphi.cocktails.web;

import com.kaaphi.cocktails.domain.RecipeElement;
import com.kaaphi.cocktails.util.Fractions;

public class RecipeElementModel {
  private final RecipeElement element;
  
  public RecipeElementModel(RecipeElement element)   {
    this.element = element;
  }
  
  public RecipeElement getElement() {
    return element;
  }
  
  public String getAmount() {
    return formatAmount(element.getAmount());
  }
  
  public boolean hasNote() {
    return !(element.getNote() == null || element.getNote().trim().isEmpty());
  }
  
  private static String formatAmount(int[] a) {
    StringBuilder sb = new StringBuilder();

    String[] parts = Fractions.toStringParts(a);
    int i = 0;
    //whole number part
    if(parts.length != 2) {
      sb.append(parts[i++]);
      if(parts.length != 1) {
        sb.append(" ");
      }
    }

    //fractional part
    if(parts.length != 1) {
      sb.append(makeFraction(parts[i++], parts[i]));
    }

    return sb.toString();
  }

  private static String makeFraction(String n, String d) {
    //format using unicode character if possible
    if("1".equals(n) && "4".equals(d)) {
      return "\u00BC";
    }

    if("1".equals(n) && "2".equals(d)) {
      return "\u00BD";
    }

    if("3".equals(n) && "4".equals(d)) {
      return "\u00BE";
    }

    //otherwise use sup/sub pair
    return String.format("<sup class=\"small\">%s</sup>\u2044<sub class=\"small\">%s</sub>", n, d);
  }
}
