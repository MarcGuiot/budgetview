package org.globsframework.model.utils;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
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
    List strings = new ArrayList();
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

  public static String dump(Glob glob) {
    StringBuilder builder = new StringBuilder();
    GlobType type = glob.getType();
    for (Field field : type.getFields()) {
      builder.append(field.getName()).append("=").append(glob.getValue(field)).append(('\n'));
    }
    return builder.toString();
  }

}
