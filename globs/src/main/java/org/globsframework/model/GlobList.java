package org.globsframework.model;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.Link;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.model.utils.GlobFieldComparator;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.Utils;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;

import java.util.*;

public class GlobList extends ArrayList<Glob> {
  public static final GlobList EMPTY = new EmptyGlobList();

  public GlobList() {
    this(new ArrayList<Glob>());
  }

  public GlobList(int capacity) {
    this(new ArrayList<Glob>(capacity));
  }

  public GlobList(Glob... globs) {
    this(Arrays.asList(globs));
  }

  public GlobList(GlobList globs) {
    super(globs);
  }

  public GlobList(Collection<Glob> globs) {
    super(new ArrayList(globs));
  }

  public void addAll(Glob... globs) {
    for (Glob glob : globs) {
      add(glob);
    }
  }

  public void addNotNull(Glob... globs) {
    for (Glob glob : globs) {
      if (glob != null) {
        add(glob);
      }
    }
  }

  public String toString() {
    List<String> strings = new ArrayList<String>();
    for (Glob glob : this) {
      if (glob == null) {
        strings.add("null");
      }
      else {
        strings.add(glob.toString());
      }
    }
    Collections.sort(strings);
    return strings.toString();
  }

  public GlobList filterSelf(GlobMatcher matcher, GlobRepository repository) {
    for (Iterator<Glob> iter = iterator(); iter.hasNext(); ) {
      Glob glob = iter.next();
      if (!matcher.matches(glob, repository)) {
        iter.remove();
      }
    }
    return this;
  }

  public GlobList filter(GlobMatcher matcher, GlobRepository repository) {
    GlobList result = new GlobList();
    for (Glob glob : this) {
      if (matcher.matches(glob, repository)) {
        result.add(glob);
      }
    }
    return result;
  }

  public GlobList getExistingGlobs(GlobRepository repository) {
    GlobList result = new GlobList();
    for (Iterator<Glob> iter = iterator(); iter.hasNext(); ) {
      Glob glob = iter.next();
      if (repository.contains(glob.getKey())) {
        result.add(glob);
      }
    }
    return result;
  }

  public void keepExistingGlobsOnly(GlobRepository repository) {
    for (Iterator<Glob> iter = iterator(); iter.hasNext(); ) {
      Glob glob = iter.next();
      if (!glob.exists() || !repository.contains(glob.getKey())) {
        iter.remove();
      }
    }
  }

  public void addAll(Collection<Key> keys, GlobRepository repository) {
    for (Key key : keys) {
      Glob glob = repository.find(key);
      if (glob != null) {
        add(glob);
      }
    }
  }

  public void removeAll(GlobMatcher matcher, GlobRepository repository) {
    for (Iterator<Glob> iter = iterator(); iter.hasNext(); ) {
      Glob glob = iter.next();
      if (matcher.matches(glob, repository)) {
        iter.remove();
      }
    }
  }

  public boolean removeAll(Set<Key> keys) {
    boolean asRemoved = false;
    for (Iterator it = this.iterator(); it.hasNext(); ) {
      Glob glob = (Glob)it.next();
      if (keys.contains(glob.getKey())) {
        it.remove();
        asRemoved = true;
      }
    }
    return asRemoved;
  }

  public GlobList subList(int fromIndex, int toIndex) {
    return new GlobList(super.subList(fromIndex, toIndex));
  }

  public GlobList clone() {
    List copy = new ArrayList(this);
    return new GlobList(copy);
  }

  public Glob[] toArray() {
    return super.toArray(new Glob[size()]);
  }

  public Key[] getKeys() {
    Key[] result = new Key[size()];
    int index = 0;
    for (Glob glob : this) {
      result[index++] = glob.getKey();
    }
    return result;
  }

  public List<Key> getKeyList() {
    List<Key> result = new ArrayList<Key>();
    for (Glob glob : this) {
      result.add(glob.getKey());
    }
    return result;
  }

  public Set<Integer> getValueSet(IntegerField field) {
    Set<Integer> result = new HashSet<Integer>();
    for (Glob glob : this) {
      result.add(glob.get(field));
    }
    return result;
  }

  public Set<Double> getValueSet(DoubleField field) {
    Set<Double> result = new HashSet<Double>();
    for (Glob glob : this) {
      result.add(glob.get(field));
    }
    return result;
  }

  public Set<String> getValueSet(StringField field) {
    Set<String> result = new HashSet<String>();
    for (Glob glob : this) {
      result.add(glob.get(field));
    }
    return result;
  }

  public Set getValueSet(Field field) {
    Set result = new HashSet();
    for (Glob glob : this) {
      result.add(glob.getValue(field));
    }
    return result;
  }

  public Double[] getValues(DoubleField field) {
    Double[] result = new Double[size()];
    int index = 0;
    for (Glob glob : this) {
      result[index++] = glob.get(field);
    }
    return result;
  }

  public Integer[] getValues(IntegerField field) {
    Integer[] result = new Integer[size()];
    int index = 0;
    for (Glob glob : this) {
      result[index++] = glob.get(field);
    }
    return result;
  }

  public SortedSet<Integer> getSortedSet(IntegerField field) {
    SortedSet<Integer> result = new TreeSet<Integer>();
    for (FieldValues values : this) {
      result.add(values.get(field));
    }
    return result;
  }

  public SortedSet<Double> getSortedSet(DoubleField field) {
    SortedSet<Double> result = new TreeSet<Double>();
    for (FieldValues values : this) {
      Double value = values.get(field);
      if (value != null) {
        result.add(value);
      }
    }
    return result;
  }

  public SortedSet<String> getSortedSet(StringField field) {
    SortedSet<String> result = new TreeSet<String>();
    for (FieldValues values : this) {
      result.add(values.get(field));
    }
    return result;
  }

  public Integer[] getSortedArray(IntegerField field) {
    SortedSet<Integer> result = getSortedSet(field);
    return result.toArray(new Integer[result.size()]);
  }

  public boolean containsValue(IntegerField field, Integer value) {
    for (Glob glob : this) {
      if (Utils.equal(glob.get(field), value)) {
        return true;
      }
    }
    return false;
  }

  public GlobList sort(Field field) {
    return sort(new GlobFieldComparator(field));
  }

  public GlobList sort(Comparator<Glob> comparator) {
    Collections.sort(this, comparator);
    return this;
  }

  public GlobList apply(GlobFunctor functor, GlobRepository repository) throws Exception {
    for (Glob glob : this) {
      functor.run(glob, repository);
    }
    return this;
  }

  public GlobList safeApply(GlobFunctor functor, GlobRepository repository) {
    try {
      apply(functor, repository);
    }
    catch (Exception e) {
      throw new UnexpectedApplicationState(e);
    }
    return this;
  }

  public List<Key> toKeyList() {
    List<Key> list = new ArrayList<Key>();
    for (Glob glob : this) {
      list.add(glob.getKey());
    }
    return list;
  }

  public Set<GlobType> getTypes() {
    Set<GlobType> types = new HashSet<GlobType>();
    for (Glob glob : this) {
      types.add(glob.getType());
    }
    return types;
  }

  public Glob getFirst() {
    if (isEmpty()) {
      return null;
    }
    return get(0);
  }

  public Glob getLast() {
    if (isEmpty()) {
      return null;
    }
    return get(size() - 1);
  }

  public GlobList getAll(GlobType type) {
    GlobList result = new GlobList();
    for (Glob glob : this) {
      if (glob.getType().equals(type)) {
        result.add(glob);
      }
    }
    return result;
  }

  public Map<Integer, Glob> toMap(IntegerField field) {
    Map<Integer, Glob> map = new HashMap<Integer, Glob>();
    for (Glob glob : this) {
      map.put(glob.get(field), glob);
    }
    return map;
  }

  public Double getSum(DoubleField field) {
    Double result = null;
    for (Glob glob : this) {
      Double value = glob.get(field);
      if (result == null) {
        result = value;
      }
      else if (value != null) {
        result += value;
      }
    }
    return result;
  }

  public GlobList getTargets(Link link, GlobRepository repository) {
    GlobList result = new GlobList();
    for (Glob glob : this) {
      Glob target = repository.findLinkTarget(glob, link);
      if (target != null) {
        result.add(target);
      }
    }
    return result;
  }
}
