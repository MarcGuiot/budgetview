package org.crossbowlabs.globs.model;
import java.util.Collection;

public class EmptyGlobList extends GlobList {

  public void addAll(Glob... globs) {
    throw new UnsupportedOperationException();
  }

  public boolean add(Glob o) {
    throw new UnsupportedOperationException();
  }

  public boolean addAll(Collection<? extends Glob> c) {
    throw new UnsupportedOperationException();
  }

  public boolean addAll(int index, Collection<? extends Glob> c) {
    throw new UnsupportedOperationException();
  }

  public void add(int index, Glob element) {
    throw new UnsupportedOperationException();
  }
}
