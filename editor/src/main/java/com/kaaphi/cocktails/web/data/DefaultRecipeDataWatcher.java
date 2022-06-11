package com.kaaphi.cocktails.web.data;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DefaultRecipeDataWatcher implements
    RecipeDataWatcher {
  private final List<RecipeDataWatcherListener> listeners = new CopyOnWriteArrayList<>();

  @Override
  public void addListener(RecipeDataWatcherListener listener) {
    listeners.add(listener);
  }

  @Override
  public void removeListener(RecipeDataWatcherListener listener) {
    listeners.remove(listener);
  }

  protected void notifyListeners() {
    listeners.forEach(RecipeDataWatcherListener::dataChanged);
  }
}
