package com.kaaphi.cocktails.dao;

import com.kaaphi.cocktails.domain.Recipe;
import java.util.Collection;
import java.util.List;

public interface RecipeDao {
  void save(Collection<Recipe> recipes) throws Exception;
  List<Recipe> load() throws Exception;
}
