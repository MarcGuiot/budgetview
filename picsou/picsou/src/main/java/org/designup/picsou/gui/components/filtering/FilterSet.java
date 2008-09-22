package org.designup.picsou.gui.components.filtering;

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
  private List<GlobMatcher> filterList = new ArrayList<GlobMatcher>();
  private Map<String, GlobMatcher> filters = new HashMap<String, GlobMatcher>();
  private List<FilterSetListener> listeners = new ArrayList<FilterSetListener>();

  public FilterSet(Filterable filterable) {
    this.filterable = filterable;
  }

  public void set(String name, GlobMatcher matcher) {
    if ((matcher == GlobMatchers.ALL) || (matcher == null)) {
      remove(name);
      notifyListeners(name, false);
    }
    filters.put(name, matcher);
    filterList.add(matcher);
    filterable.setFilter(matcher);
    notifyListeners(name, true);
  }

  public void replaceAllWith(String name, GlobMatcher matcher) {
    for (String filterName : filters.keySet()) {
      if (!filterName.equals(name)) {
        notifyListeners(filterName, false);
      }
    }
    filters.clear();
    filterList.clear();
    set(name, matcher);
  }

  public void remove(String name) {
    if (!filters.containsKey(name)) {
      return;
    }
    filters.remove(name);
    filterList.remove(filters.get(name));
    filterable.setFilter(filters.isEmpty() ? GlobMatchers.ALL : this);
    notifyListeners(name, false);    
  }

  public void clear() {
    filters.clear();
    filterList.clear();
    filterable.setFilter(GlobMatchers.ALL);
    for (String filterName : filters.keySet()) {
      notifyListeners(filterName, false);
    }
  }

  public boolean matches(Glob item, GlobRepository repository) {
    for (GlobMatcher matcher : filterList) {
      if (!matcher.matches(item, repository)) {
        return false;
      }
    }
    return true;
  }

  public void addListener(FilterSetListener listener) {
    this.listeners.add(listener);
  }

  public void removeListener(FilterSetListener listener) {
    this.listeners.remove(listener);
  }

  private void notifyListeners(String name, boolean enabled) {
    for (FilterSetListener listener : listeners) {
      listener.filterUpdated(name, enabled);
    }
  }
}
