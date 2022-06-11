package com.kaaphi.cocktails.web.data;

public interface RecipeDataWatcher {
  void addListener(RecipeDataWatcherListener listener);
  void removeListener(RecipeDataWatcherListener listener);

  interface RecipeDataWatcherListener {
    void dataChanged();
  }
}
