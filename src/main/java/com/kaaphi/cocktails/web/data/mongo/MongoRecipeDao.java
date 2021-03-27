package com.kaaphi.cocktails.web.data.mongo;

import static com.mongodb.client.model.Filters.eq;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kaaphi.cocktails.dao.RecipeDao;
import com.kaaphi.cocktails.domain.Recipe;
import com.kaaphi.cocktails.web.MongoRecipeDaoModule.MongoRecipeCollection;
import com.kaaphi.cocktails.web.MongoRecipeDaoModule.MongoUpdateLog;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoRecipeDao implements RecipeDao {
  private static final Logger log = LoggerFactory.getLogger(MongoRecipeDao.class);

  private MongoCollection<Recipe> recipeCollection;
  private MongoCollection<Document> updateLog;

  @Inject
  public MongoRecipeDao(@MongoRecipeCollection MongoCollection<Recipe> recipeCollection, @MongoUpdateLog MongoCollection<Document> updateLog) {
    this.recipeCollection = recipeCollection;
    this.updateLog = updateLog;
  }

  @Override
  public void save(Collection<Recipe> recipes) throws Exception {
    for(Recipe r : recipes) {
      if(r.getId() != null) {
        UpdateResult updateResult = recipeCollection.replaceOne(eq("_id", r.getId()), r);
        log.trace("Matched {}, updated {}, value {}", updateResult.getMatchedCount(), updateResult.getModifiedCount(), updateResult.getUpsertedId());
      } else {
        InsertOneResult result = recipeCollection.insertOne(r);
        r.setId(result.getInsertedId().asObjectId().getValue());
      }
    }

    updateLog.insertOne(new Document().append("update", Instant.now()));
  }

  @Override
  public List<Recipe> load() throws Exception {
    List<Recipe> recipes = new LinkedList<>();
    recipeCollection.find()
        .forEach(recipes::add);

    recipes.forEach(r -> log.trace("Recipe {} {}", r.getId(), r.getName()));
    return recipes;
  }
}
