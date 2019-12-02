package com.kaaphi.cocktails.ui;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashSet;
import java.util.Set;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public interface DirtyListener {
  public void dirtyStateChange(Object source, boolean isDirty);

  public static class DirtySupport implements DocumentListener, ItemListener, DirtyListener {
    private Set<DirtyListener> listeners = new HashSet<DirtyListener>();
    private Object source;
    private boolean suppress = false;

    public DirtySupport(Object source) {
      this.source = source;			
    }

    public void addDirtyListener(DirtyListener l) {
      listeners.add(l);
    }

    public void removeDirtyListener(DirtyListener l) {
      listeners.remove(l);
    }

    public void setSuppressEvents(boolean flag) {
      this.suppress = flag;
    }

    public void fireDirtyChanged(boolean isDirty) {
      if(!suppress) {
        for(DirtyListener l : listeners) {
          l.dirtyStateChange(source, isDirty);
        }
      }
    }

    @Override
    public void changedUpdate(DocumentEvent paramDocumentEvent) {
      fireDirtyChanged(true);
    }

    @Override
    public void insertUpdate(DocumentEvent paramDocumentEvent) {
      fireDirtyChanged(true);
    }

    @Override
    public void removeUpdate(DocumentEvent paramDocumentEvent) {
      fireDirtyChanged(true);
    }

    @Override
    public void dirtyStateChange(Object source, boolean isDirty) {
      if(isDirty)
        fireDirtyChanged(true);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
      fireDirtyChanged(true);
    }
  }
}
