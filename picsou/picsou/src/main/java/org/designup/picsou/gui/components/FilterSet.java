package org.designup.picsou.gui.components;

import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilterSet implements GlobMatcher {
  private Filterable filterable;
  private PicsouTableHeaderPainter headerPainter;
  private List<GlobMatcher> filterList = new ArrayList<GlobMatcher>();
  private Map<String, GlobMatcher> filters = new HashMap<String, GlobMatcher>();

  public FilterSet(Filterable filterable, PicsouTableHeaderPainter headerPainter) {
    this.filterable = filterable;
    this.headerPainter = headerPainter;
  }

  public void set(String name, GlobMatcher matcher) {
    if (matcher == GlobMatchers.ALL) {
      remove(name);
    }
    filters.put(name, matcher);
    filterList.add(matcher);
    filterable.setFilter(matcher);
    headerPainter.setFiltered(true);
  }

  public void replaceAllWith(String name, GlobMatcher matcher) {
    filters.clear();
    filterList.clear();
    set(name, matcher);
  }

  public void remove(String name) {
    filters.remove(name);
    filterList.remove(filters.get(name));
    filterable.setFilter(filters.isEmpty() ? GlobMatchers.ALL : this);
    headerPainter.setFiltered(!filters.isEmpty());
  }

  public boolean matches(Glob item, GlobRepository repository) {
    for (GlobMatcher matcher : filterList) {
      if (!matcher.matches(item, repository)) {
        return false;
      }
    }
    return true;
  }
}
