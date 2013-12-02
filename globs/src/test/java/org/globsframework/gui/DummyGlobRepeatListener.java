package org.globsframework.gui;

import org.globsframework.gui.components.GlobRepeatListener;
import org.globsframework.model.GlobList;
import org.globsframework.metamodel.DummyObject;

import java.util.SortedSet;

import junit.framework.Assert;

public class DummyGlobRepeatListener implements GlobRepeatListener {

  private StringBuilder builder = new StringBuilder();

  public void listChanged(GlobList currentList) {
    SortedSet<String> names = currentList.getSortedSet(DummyObject.NAME);
    if (builder.length() > 0) {
      builder.append('\n');
    }
    boolean first = true;
    for (String name : names) {
      if (first) {
        first = false;
      }
      else {
        builder.append(',');
      }
      builder.append(name);
    }
  }

  public void check(String text) {
    Assert.assertEquals(text, builder.toString());
    builder = new StringBuilder();
  }
}
