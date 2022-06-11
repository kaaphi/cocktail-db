package com.kaaphi.cocktails.web.data.mongo;

import com.google.inject.Inject;
import com.kaaphi.cocktails.web.data.DefaultRecipeDataWatcher;
import com.mongodb.CursorType;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import java.time.Instant;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoRecipeDataWatcher extends DefaultRecipeDataWatcher {
  private static final Logger log = LoggerFactory.getLogger(MongoRecipeDao.class);

  private final MongoCollection<Document> updateLog;
  private final Thread thread;
  private volatile boolean stopped;

  public MongoRecipeDataWatcher(MongoCollection<Document> updateLog) {
    this.updateLog = updateLog;
    thread = new Thread(this::watch, "MongoRecipeDataWatcher");
    thread.setDaemon(true);
  }

  public void start() {
    thread.start();
  }

  public void stop() {
    stopped = true;
    thread.interrupt();
  }

  private void watch() {
    log.info("Started");
    while (!stopped) {
      MongoCursor<Document> cursor = updateLog.find(Filters.gt("update", Instant.now()))
          .cursorType(CursorType.TailableAwait)
          .noCursorTimeout(true)
          .iterator();


      log.trace("Starting cursor loop");
      while (cursor.hasNext()) {
        log.trace("Waiting...");
        Document result = cursor.next();
        log.debug("Updated: {}", result);
        notifyListeners();
      }
      log.trace("Cursor ended");
      try {
        Thread.sleep(2000);
      } catch (InterruptedException e) {
        log.debug("Interrupted.");
      }
    }
    log.info("Watch thread ended.");
  }
}
