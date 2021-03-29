package com.kaaphi.cocktails.web;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.bson.codecs.configuration.CodecRegistries.fromCodecs;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.kaaphi.cocktails.dao.RecipeDao;
import com.kaaphi.cocktails.domain.Recipe;
import com.kaaphi.cocktails.web.data.RecipeDataWatcher;
import com.kaaphi.cocktails.web.data.mongo.MongoRecipeDao;
import com.kaaphi.cocktails.web.data.mongo.MongoRecipeDataWatcher;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CreateCollectionOptions;
import java.lang.annotation.Retention;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import javax.inject.Qualifier;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.Conventions;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoRecipeDaoModule extends AbstractModule {
  private static final Logger log = LoggerFactory.getLogger(MongoRecipeDaoModule.class);

  @Qualifier
  @Retention(RUNTIME)
  public @interface MongoUpdateLog {}

  @Qualifier
  @Retention(RUNTIME)
  public @interface MongoRecipeCollection {}

  @Override
  public void configure() {
    bind(RecipeDao.class).to(MongoRecipeDao.class);
  }

  @Provides @Singleton
  RecipeDataWatcher provideDataWatcher(@MongoUpdateLog MongoCollection<Document> updateLog) {
    MongoRecipeDataWatcher dataWatcher = new MongoRecipeDataWatcher(updateLog);
    dataWatcher.start();
    return dataWatcher;
  }

  @Provides @MongoRecipeCollection
  MongoCollection<Recipe> provideRecipeCollection(MongoDatabase database) {
    return database.getCollection("cocktailRecipes", Recipe.class);
  }

  @Provides @MongoUpdateLog
  MongoCollection<Document> provideUpdateLog(MongoDatabase database) {
    return database.getCollection("updateLog", Document.class);
  }

  @Provides @Singleton
  MongoDatabase provideDatabase(MongoClient client) {
    MongoDatabase database = client.getDatabase("cocktailDb");

    Set<String> collectionNames = new HashSet<>();
    database.listCollectionNames().into(collectionNames);
    if (!collectionNames.contains("updateLog")) {
      database.createCollection("updateLog", new CreateCollectionOptions()
          .maxDocuments(10)
          .capped(true)
          .sizeInBytes(256));

      database.getCollection("updateLog").insertOne(new Document().append("update", Instant.now()));
    }

    return database;
  }

  @Provides
  MongoClient provideClient(@Named("mongo.connectionString") String connectionString) {
    CodecRegistry pojoCodecRegistry = fromRegistries(fromCodecs(new IntArrayCodec()),
        MongoClientSettings.getDefaultCodecRegistry(),
        fromProviders(PojoCodecProvider.builder()
            .conventions(Conventions.DEFAULT_CONVENTIONS)
            .automatic(true).build()));

    MongoClientSettings.Builder settings = MongoClientSettings.builder()
        .codecRegistry(pojoCodecRegistry);

    if(connectionString != null && !connectionString.isEmpty()) {
      log.info("Using connection string {}", redactConnectionString(connectionString));
      settings.applyConnectionString(new ConnectionString(connectionString));
    }

    return MongoClients.create(settings.build());
  }

  private static final Pattern REDACT_PATTERN = Pattern.compile("mongodb://(.*?):(.*?)@");
  private static String redactConnectionString(String connectionString) {
    Matcher m = REDACT_PATTERN.matcher(connectionString);
    StringBuilder sb = new StringBuilder();
    if(m.find()) {
      m.appendReplacement(sb, "mongodb://$1:*****@");
    }
    m.appendTail(sb);
    return sb.toString();
  }

  private static class IntArrayCodec implements Codec<int[]> {

    @Override
    public int[] decode(BsonReader reader, DecoderContext decoderContext) {
      IntStream.Builder b = IntStream.builder();
      reader.readStartArray();

      while(reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
        b.add(reader.readInt32());
      }

      reader.readEndArray();

      return b.build().toArray();
    }

    @Override
    public void encode(BsonWriter writer, int[] value, EncoderContext encoderContext) {
      writer.writeStartArray();
      IntStream.of(value).forEach(writer::writeInt32);
      writer.writeEndArray();
    }

    @Override
    public Class<int[]> getEncoderClass() {
      return int[].class;
    }
  }
}
