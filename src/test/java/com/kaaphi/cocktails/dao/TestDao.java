package com.kaaphi.cocktails.dao;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.kaaphi.cocktails.domain.Recipe;
import com.kaaphi.cocktails.domain.RecipeElement;

public class TestDao {
	public static void main(String[] args) throws Exception {
		//DataSource ds = new SQLiteDataSource("test.db");
		
		RecipeDao dao = new CustomFormatRecipeDao(new File("/home/kaaphi/cocktail_db/db.dat"));
		/*
		Recipe r = new Recipe("Test", "Mix it", "test ref", "a note");
		r.addRecipeElement(new RecipeElement("Rum", 1, "oz", "good rum", r));
		
		dao.save(Arrays.asList(r));
		*/
		
		List<Recipe> recipes = dao.load();
		
		for(Recipe r : recipes) {
			for(RecipeElement e : r.getRecipeElements()) {
				if(e.getNote().contains("Regan")) {
					System.out.format("%s: %s%n", r, e);
				}
			}
		}
		
		//dao.save(recipes);
	}
}
