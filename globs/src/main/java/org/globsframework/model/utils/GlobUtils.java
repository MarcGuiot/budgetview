package org.globsframework.model.utils;

import org.globsframework.metamodel.Link;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.*;
import org.globsframework.utils.Utils;

import java.util.*;

public class GlobUtils {

  public static Double safeGet(Glob glob, DoubleField field) {
    if (glob == null) {
      return null;
    }
    return glob.get(field);
  }

  public static SortedSet<Integer> getSortedValues(FieldValues[] valuesList, IntegerField field) {
    SortedSet<Integer> result = new TreeSet<Integer>();
    for (FieldValues values : valuesList) {
      result.add(values.get(field));
    }
    return result;
  }

  public static void setValue(Glob glob, Double value, GlobRepository repository, DoubleField... fields) {
    repository.startChangeSet();
    try {
      for (DoubleField field : fields) {
        repository.update(glob.getKey(), field, value);
      }
    }
    finally {
      repository.completeChangeSet();
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

    void move(int previousIndex, int newIndex);
  }

  public static <T> void diff(List<T> from, List<T> to, DiffFunctor<T> functor) {
    T[] fromArray = (T[])from.toArray(new Object[from.size() + to.size()]);
    int toPos = 0;

    T fromT = fromArray.length == 0 ? null : fromArray[0];
    for (T element : to) {
      if (!Utils.equal(element, fromT)) {
        boolean moved = false;
        for (int i = toPos + 1; i < fromArray.length; i++) {
          T t = fromArray[i];
          if (t != null && t.equals(element)) {
            functor.move(i, toPos);
            System.arraycopy(fromArray, toPos, fromArray, toPos + 1, i - toPos);
            fromArray[toPos] = t;
            moved = true;
            break;
          }
        }

        if (!moved) {
          functor.add(element, toPos);
          System.arraycopy(fromArray, toPos, fromArray, toPos + 1, fromArray.length - toPos - 1);
          fromArray[toPos] = element;
        }
      }
      toPos++;
      if (toPos < fromArray.length) {
        fromT = fromArray[toPos];
      }
      else {
        fromT = null;
      }
    }
    for (int index = fromArray.length; index > to.size(); index--) {
      if (fromArray[index - 1] != null) {
        functor.remove(index - 1);
      }
    }
  }
}
