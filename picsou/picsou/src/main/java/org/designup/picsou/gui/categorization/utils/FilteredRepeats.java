package org.designup.picsou.gui.categorization.utils;

import org.designup.picsou.gui.utils.Matchers;
import org.globsframework.gui.utils.GlobRepeat;
import org.globsframework.utils.collections.Pair;

import java.util.ArrayList;
import java.util.Iterator;

public class FilteredRepeats implements Iterable<Pair<Matchers.CategorizationFilter, GlobRepeat>> {

  java.util.List<Pair<Matchers.CategorizationFilter, GlobRepeat>> list =
    new ArrayList<Pair<Matchers.CategorizationFilter, GlobRepeat>>();

  public Iterator<Pair<Matchers.CategorizationFilter, GlobRepeat>> iterator() {
    return list.iterator();
  }

  public void add(Matchers.CategorizationFilter filter, GlobRepeat repeat) {
    list.add(new Pair<Matchers.CategorizationFilter, GlobRepeat>(filter, repeat));
  }
}
