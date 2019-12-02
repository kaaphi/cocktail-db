package com.kaaphi.cocktails.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import javax.swing.ListModel;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class SortedListModel<T> implements ListModel, Iterable<T> {
  private EventListenerList listenerList;
  private List<T> objects;
  private Comparator<? super T> comparator;

  public SortedListModel(Comparator<? super T> c) {
    this.comparator = c;
    this.objects = new ArrayList<T>();
    this.listenerList = new EventListenerList();
  }

  @Override
  public void addListDataListener(ListDataListener l) {
    listenerList.add(ListDataListener.class, l);
  }

  @Override
  public Object getElementAt(int i) {
    return objects.get(i);
  }

  @Override
  public int getSize() {
    return objects.size();
  }

  @Override
  public void removeListDataListener(ListDataListener l) {
    listenerList.remove(ListDataListener.class, l);
  }

  public void clearObjects() {
    int prevSize = getSize();
    this.objects.clear();
    fireIntervalRemoved(this, 0, prevSize);
  }


  public void setObjects(Collection<T> objects) {
    clearObjects();
    this.objects = new ArrayList<T>(objects);

    Collections.sort(this.objects, comparator);

    fireIntervalAdded(this, 0, getSize());
  }

  public void addObject(T object) {
    int i = Collections.binarySearch(objects, object, comparator);

    if(i< 0) {
      i = -i- 1;
    }
    objects.add(i, object);

    fireIntervalAdded(this, i, i);
  }

  public void removeObject(T object) {
    int i = objects.indexOf(object);
    objects.remove(i);

    fireIntervalRemoved(this, i, i);
  }


  protected void fireContentsChanged(Object source, int index0, int index1)
  {
    Object[] listeners = this.listenerList.getListenerList();
    ListDataEvent e = null;

    for (int i = listeners.length - 2; i >= 0; i -= 2)
      if (listeners[i] == ListDataListener.class) {
        if (e == null) {
          e = new ListDataEvent(source, 0, index0, index1);
        }
        ((ListDataListener)listeners[(i + 1)]).contentsChanged(e);
      }
  }

  protected void fireIntervalAdded(Object source, int index0, int index1)
  {
    Object[] listeners = this.listenerList.getListenerList();
    ListDataEvent e = null;

    for (int i = listeners.length - 2; i >= 0; i -= 2)
      if (listeners[i] == ListDataListener.class) {
        if (e == null) {
          e = new ListDataEvent(source, 1, index0, index1);
        }
        ((ListDataListener)listeners[(i + 1)]).intervalAdded(e);
      }
  }

  protected void fireIntervalRemoved(Object source, int index0, int index1)
  {
    Object[] listeners = this.listenerList.getListenerList();
    ListDataEvent e = null;

    for (int i = listeners.length - 2; i >= 0; i -= 2)
      if (listeners[i] == ListDataListener.class) {
        if (e == null) {
          e = new ListDataEvent(source, 2, index0, index1);
        }
        ((ListDataListener)listeners[(i + 1)]).intervalRemoved(e);
      }
  }

  @Override
  public Iterator<T> iterator() {
    return objects.iterator();
  }

}
