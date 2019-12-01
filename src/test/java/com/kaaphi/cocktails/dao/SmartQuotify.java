package com.kaaphi.cocktails.dao;

import java.io.File;
import java.util.List;

import com.kaaphi.cocktails.domain.Recipe;

public class SmartQuotify {
	public static void main(String[] args) throws Exception {
		
		File file = new File("/home/kaaphi/cocktail_db/db.dat");
		
		CustomFormatRecipeDao dao = new CustomFormatRecipeDao(file);
		List<Recipe> rs = dao.load();
		
		/*
		for(Recipe r : rs) {
			r.setName(quotify(r.getName()));
			r.setInstructions(quotify(r.getInstructions()));
			r.setNote(quotify(r.getNote()));
			
			for(RecipeElement e : r.getRecipeElements()) {
				e.setIngredient(quotify(e.getIngredient()));
				e.setNote(quotify(e.getNote()));
			}
		}
		 */
		
		for(Recipe r : rs) {
			if(r.getTags().remove("untried")) {
				r.getTags().add("newish");
			}
		}
		
		dao.save(rs);
	}
	
	private static String quotify(String s) {
		return s
		.replaceAll("(?<=\\w)'", "\u2019")
		.replaceAll("(?<=\\s)'(?=\\w)", "\u2018")
		.replaceAll("(?<=\\w)\"(?!\\w)", "\u201D")
		.replaceAll("(?<=\\s)\"(?=\\w)", "\u201C")
		;
	}
	
}
