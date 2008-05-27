package org.crossbowlabs.globs.model.utils;

import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.GlobList;

import java.util.*;

public class SortedGlobList implements Iterable<Glob> {
  private List<Glob> list = new ArrayList<Glob>();
  private Comparator<Glob> comparator;

  public SortedGlobList(Comparator<Glob> comparator) {
    this.comparator = comparator;
  }

  public int add(Glob value) {
    int index = indexOf(value);
    if (index < 0) {
      index = -index - 1;
    }
    list.add(index, value);
    return index;
  }

  public void addAll(Collection<Glob> all) {
    list.addAll(all);
    updateSorting();
  }

  public void remove(int index) {
    list.remove(index);
  }

  public int remove(Glob value) {
    int index = indexOf(value);
    list.remove(value);
    return index;
  }

  public void removeAll(List<Glob> toRemove) {
    list.removeAll(toRemove);
    updateSorting();
  }

  public Iterator<Glob> iterator() {
    return list.iterator();
  }

  public Glob get(int index) {
    return list.get(index);
  }

  public int indexOf(Glob value) {
    return Collections.binarySearch(list, value, comparator);
  }

  public int firstIndexOf(GlobMatcher matcher, GlobRepository repository) {
    int index = 0;
    for (Glob item : list) {
      if (matcher.matches(item, repository)) {
        return index;
      }
      index++;
    }
    return -1;
  }

  public Glob getFirst(GlobMatcher matcher, GlobRepository repository) {
    for (Glob item : list) {
      if (matcher.matches(item, repository)) {
        return item;
      }
    }
    return null;
  }

  public int size() {
    return list.size();
  }

  public void clear() {
    list.clear();
  }

  public boolean isEmpty() {
    return list.isEmpty();
  }

  public void setComparator(Comparator<Glob> comparator) {
    this.comparator = comparator;
    updateSorting();
  }

  public void updateSorting() {
    Collections.sort(list, comparator);
  }

  public String toString() {
    return list.toString();
  }

  public GlobList asList() {
    return new GlobList(list);
  }
}
