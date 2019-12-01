package com.kaaphi.cocktails.domain;

import java.util.Arrays;
import java.util.List;

public class AutoTag {
	private static final List<String> standard_ingredients = Arrays.asList(
		"Gin",
		"Rum",
		"Dark Rum",
		"Sweet Vermouth",
		"Dry Vermouth",
		"Aromatic Bitters",
		"Peychaud’s Bitters",
		"Angostura Bitters",
		"Orange Bitters",
		"Campari",
		"Rye Whiskey",
		"Bourbon",
		"Orange Juice",
		"Lime Juice",
		"Lemon Juice",
		"Absinthe",
		"Sugar",
		"Simple Syrup",
		"Grenadine",
		"Cointreau",
		"Tequila",
		"Maraschino",
		"Vodka",
		"Curaçao",
		"Bénédictine"		
	);
	
	private static final List<String> refreshing_ingredients = Arrays.asList(
			"Ginger Ale",
			"Ginger Beer",
			"Soda Water",
			"Tonic Water"
		);
	
	private static final List<? extends Tag> tags = Arrays.asList(
			new Tag("standard") {
				public boolean fitsTag(Recipe r) {
					for(RecipeElement e : r.getRecipeElements()) {
						if(!standard_ingredients.contains(e.getIngredient())) {
							return false;
						}
					}
					return true;
				}
			},
			new Tag("refreshing") {
				public boolean fitsTag(Recipe r) {
					for(RecipeElement e : r.getRecipeElements()) {
						if(refreshing_ingredients.contains(e.getIngredient())) {
							return true;
						}
					}
					return false;
				}
			}
	);
	
	public static void autoTag(Recipe r) {
		for(Tag t : tags) {
			t.addTagTo(r);
		}
	}
	
	private static abstract class Tag {
		private String tag;
		
		public Tag(String tag) {
			this.tag = tag;
		}
		
		public void addTagTo(Recipe r) {
			if(fitsTag(r)) {
				List<String> tags = r.getTags();
				
				if(!tags.contains(tag)) {
					tags.add(tag);
				}
			}
		}
		
		protected abstract boolean fitsTag(Recipe r);
	}
}
