package com.kaaphi.cocktails.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.kaaphi.cocktails.domain.Recipe;

public class SerializationRecipeDao implements RecipeDao {
	private File file;
	
	public SerializationRecipeDao(File file) {
		this.file = file;
	}
		
	@Override
	public List<Recipe> load() throws Exception {
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
		try {
			return (List<Recipe>)in.readObject();
		} finally {
			in.close();
		}
	}

	@Override
	public void save(Collection<Recipe> recipes) throws Exception {
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
		try {
			out.writeObject(new ArrayList<Recipe>(recipes));
		} finally {
			out.close();
		}
	}

}
