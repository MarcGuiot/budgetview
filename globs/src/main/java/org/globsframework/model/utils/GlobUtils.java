package org.globsframework.model.utils;

import org.globsframework.metamodel.Link;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.*;

import java.util.*;

public class GlobUtils {

  public static SortedSet<Integer> getSortedValues(FieldValues[] valuesList, IntegerField field) {
    SortedSet<Integer> result = new TreeSet<Integer>();
    for (FieldValues values : valuesList) {
      result.add(values.get(field));
    }
    return result;
  }

  public static void setValue(Glob glob, Double value, GlobRepository repository, DoubleField... fields) {
    repository.enterBulkDispatchingMode();
    try {
      for (DoubleField field : fields) {
        repository.update(glob.getKey(), field, value);
      }
    }
    finally {
      repository.completeBulkDispatchingMode();
    }
  }

  public static String toString(Glob[] globs) {
    List<String> strings = new ArrayList<String>();
    for (Glob glob : globs) {
      strings.add(glob.getKey().toString());
    }
    Collections.sort(strings);
    return strings.toString();
  }

  public static void add(Glob glob, DoubleField field, double value, GlobRepository repository) {
    Key key = glob.getKey();
    add(key, glob, field, value, repository);
  }

  public static void add(Key key, Glob glob, DoubleField field, double value, GlobRepository repository) {
    Double currentValue = glob.get(field);
    if (currentValue == null) {
      repository.update(key, field, value);
      return;
    }
    if (value != 0) {
      double newValue = currentValue + value;
      repository.update(key, field, newValue);
    }
  }

  public static GlobList getTargets(GlobList from, Link link, GlobRepository repository) {
    GlobList result = new GlobList();
    for (Glob glob : from) {
      Glob target = repository.findLinkTarget(glob, link);
      if (target != null) {
        result.add(target);
      }
    }
    return result;
  }

  public interface DiffFunctor<T> {
    void add(T glob, int index);

    void remove(int index);
  }

  public static <T> void diff(List<T> from, List<T> to, DiffFunctor<T> functor) {
    Iterator<T> fromGlobs = from.iterator();
    int toPos = 0;
    int added = 0;

    T fromGlob = (T)(fromGlobs.hasNext() ? fromGlobs.next() : null);
    for (T glob : to) {
      if (glob != fromGlob) {
        functor.add(glob, toPos);
        added++;
      }
      else {
        fromGlob = (T)(fromGlobs.hasNext() ? fromGlobs.next() : null);
      }
      toPos++;
    }
    for (int index = from.size() + added; index > to.size(); index--) {
      functor.remove(index - 1);
    }
  }
}
