package com.kaaphi.cocktails.web;

import java.util.Objects;

public class CategoryModel implements Comparable<CategoryModel> {
  private final String name;
  private final String anchor;
  public CategoryModel(String name, String anchor) {
    super();
    this.name = name;
    this.anchor = anchor;
  }
  @Override
  public int hashCode() {
    return Objects.hash(anchor, name);
  }
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof CategoryModel)) {
      return false;
    }
    CategoryModel other = (CategoryModel) obj;
    return Objects.equals(anchor, other.anchor) && Objects.equals(name, other.name);
  }
  public String getName() {
    return name;
  }
  public String getAnchor() {
    return anchor;
  }
  @Override
  public int compareTo(CategoryModel o) {
    return this.name.compareTo(o.name);
  }
}
