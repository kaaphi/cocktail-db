package com.kaaphi.cocktails.dao;

import java.util.Collection;
import java.util.List;

import com.kaaphi.cocktails.domain.Recipe;

public interface RecipeDao {
	public void save(Collection<Recipe> recipes) throws Exception;
	public List<Recipe> load() throws Exception;
}
