package org.globsframework.model.utils;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.Link;
import org.globsframework.metamodel.fields.*;
import org.globsframework.metamodel.links.FieldMappingFunctor;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.Strings;
import org.globsframework.utils.Utils;

import java.util.*;

public class GlobMatchers {

  public static GlobMatcher ALL = new GlobMatcher() {
    public boolean matches(Glob item, GlobRepository repository) {
      return true;
    }

    public String toString() {
      return "all";
    }
  };

  public static GlobMatcher NONE = new GlobMatcher() {
    public boolean matches(Glob item, GlobRepository repository) {
      return false;
    }

    public String toString() {
      return "none";
    }
  };

  public static GlobMatcher fieldEquals(IntegerField field, Integer value) {
    return fieldEqualsObject(field, value);
  }

  public static GlobMatcher fieldIn(final IntegerField field, final Integer... values) {
    if (values.length == 0) {
      return GlobMatchers.NONE;
    }
    return new GlobMatcher() {
      public boolean matches(Glob item, GlobRepository repository) {
        Integer fieldValue = item.get(field);
        for (Integer value : values) {
          if (Utils.equal(fieldValue, value)) {
            return true;
          }
        }
        return false;
      }
    };
  }

  public static GlobMatcher fieldEquals(LinkField field, Integer value) {
    return fieldEqualsObject(field, value);
  }

  public static GlobMatcher fieldEquals(StringField field, String value) {
    return fieldEqualsObject(field, value);
  }

  public static GlobMatcher fieldEqualsIgnoreCase(final StringField field, final String value) {
    return new GlobMatcher() {
      public boolean matches(Glob glob, GlobRepository repository) {
        return Utils.equalIgnoreCase(glob.get(field), value);
      }
    };
  }

  public static GlobMatcher fieldEquals(DoubleField field, Double value) {
    return fieldEqualsObject(field, value);
  }

  public static GlobMatcher fieldEquals(BlobField field, byte[] value) {
    return fieldEqualsObject(field, value);
  }

  public static GlobMatcher fieldEquals(DateField field, Date value) {
    return fieldEqualsObject(field, value);
  }

  public static GlobMatcher isTrue(BooleanField field) {
    return fieldEqualsObject(field, true);
  }

  public static GlobMatcher isFalse(BooleanField field) {
    return fieldEqualsObject(field, false);
  }

  public static GlobMatcher fieldEquals(BooleanField field, Boolean value) {
    return fieldEqualsObject(field, value);
  }

  public static GlobMatcher fieldEquals(LongField field, Long value) {
    return fieldEqualsObject(field, value);
  }

  public static GlobMatcher fieldEqualsObject(Field field, Object value) {
    return new SingleFieldMatcher(field, value);
  }

  public static GlobMatcher fieldContainsIgnoreCase(final StringField field, final String value) {
    if (Strings.isNullOrEmpty(value)) {
      return ALL;
    }
    return new GlobMatcher() {
      public boolean matches(Glob item, GlobRepository repository) {
        String actual = item.get(field);
        return actual != null && actual.toLowerCase().contains(value.toLowerCase());
      }
    };
  }

  public static GlobMatcher contained(Field field, Object... values) {
    return contained(field, Arrays.asList(values));
  }

  public static GlobMatcher contained(Field field, Collection values) {
    if ((values == null) || values.isEmpty()) {
      return NONE;
    }
    if (values.size() == 1) {
      return new SingleFieldMatcher(field, values.iterator().next());
    }
    return new CollectionFieldMatcher(field, values);
  }

  /**
   * Accepts all Globs who are linked to a given Glob.
   */
  public static GlobMatcher linkedTo(final Glob target, final Link link) {
    if (target == null) {
      return NONE;
    }
    final List<Field> sourceFields = new ArrayList<Field>();
    final List<Field> targetFields = new ArrayList<Field>();
    link.apply(new FieldMappingFunctor() {
      public void process(Field sourceField, Field targetField) {
        sourceFields.add(sourceField);
        targetFields.add(targetField);
      }
    });
    final Object targetValue[] = new Object[sourceFields.size()];
    int i = 0;
    for (Field field : targetFields) {
      targetValue[i] = target.getValue(field);
      i++;
    }
    final Field sourceFielsArray[] = sourceFields.toArray(new Field[sourceFields.size()]);
    return new GlobMatcher() {
      public boolean matches(final Glob item, GlobRepository repository) {
        for (int j = 0; j < sourceFielsArray.length; j++) {
          Field field = sourceFielsArray[j];
          if (!Utils.equal(targetValue[j], item.getValue(field))) {
            return false;
          }
        }
        return true;
      }
    };
  }

  public static GlobMatcher linkedTo(Glob target, final LinkField link) {
    if (target == null) {
      return NONE;
    }
    final Integer targetvalue = target.get(link.getTargetKeyField());
    return fieldEquals(link, targetvalue);
  }

  public static GlobMatcher linkedTo(Key target, final LinkField link) {
    if (target == null) {
      return NONE;
    }
    final Integer targetvalue = target.get(link.getTargetKeyField());
    return fieldEquals(link, targetvalue);
  }

  public static GlobMatcher linkTargetFieldEquals(final Link link, final Field targetField,
                                                  final Object targetFieldValue) {
    return new GlobMatcher() {
      public boolean matches(Glob item, GlobRepository repository) {
        final Glob target = repository.findLinkTarget(item, link);
        if (target == null) {
          return false;
        }
        return target.getValue(targetField).equals(targetFieldValue);
      }
    };
  }

  public static GlobMatcher linkedTo(final Link link, final GlobMatcher linkedObjectMatcher) {
    return new GlobMatcher() {
      public boolean matches(Glob item, GlobRepository repository) {
        Glob target = repository.findLinkTarget(item, link);
        return (target != null) && linkedObjectMatcher.matches(target, repository);
      }
    };
  }


  public static GlobMatcher isNull(final Field field) {
    return new GlobMatcher() {
      public boolean matches(Glob glob, GlobRepository repository) {
        return glob.getValue(field) == null;
      }
    };
  }

  public static GlobMatcher isNotNull(final Field field) {
    return new GlobMatcher() {
      public boolean matches(Glob item, GlobRepository repository) {
        return item.getValue(field) != null;
      }
    };
  }

  public static GlobMatcher isNullOrEmpty(final StringField field) {
    return new GlobMatcher() {
      public boolean matches(Glob item, GlobRepository repository) {
        return Strings.isNullOrEmpty(item.get(field));
      }
    };
  }

  public static GlobMatcher isNotEmpty(final StringField field) {
    return new GlobMatcher() {
      public boolean matches(Glob item, GlobRepository repository) {
        return Strings.isNotEmpty(item.get(field));
      }
    };
  }

  public static GlobMatcher and(final GlobMatcher... matchers) {
    for (GlobMatcher matcher : matchers) {
      if (matcher.equals(NONE)) {
        return NONE;
      }
    }
    List<GlobMatcher> significantMatchers = new ArrayList<GlobMatcher>();
    for (GlobMatcher matcher : matchers) {
      if ((matcher == null) || (matcher != ALL)) {
        significantMatchers.add(matcher);
      }
    }
    if (significantMatchers.isEmpty()) {
      return ALL;
    }
    if (significantMatchers.size() == 1) {
      return significantMatchers.get(0);
    }
    return new GlobMatcher() {
      public boolean matches(Glob item, GlobRepository repository) {
        for (GlobMatcher matcher : matchers) {
          if (!matcher.matches(item, repository)) {
            return false;
          }
        }
        return true;
      }
    };
  }

  public static GlobMatcher or(final GlobMatcher... matchers) {
    List<GlobMatcher> significantMatchers = new ArrayList<GlobMatcher>();
    for (GlobMatcher matcher : matchers) {
      if ((matcher != null) && matcher.equals(ALL)) {
        return ALL;
      }
      if ((matcher != null) && (matcher != NONE)) {
        significantMatchers.add(matcher);
      }
    }
    if (significantMatchers.isEmpty()) {
      return ALL;
    }
    if (significantMatchers.size() == 1) {
      return significantMatchers.get(0);
    }
    return new GlobMatcher() {
      public boolean matches(Glob glob, GlobRepository repository) {
        for (GlobMatcher matcher : matchers) {
          if (matcher.matches(glob, repository)) {
            return true;
          }
        }
        return false;
      }
    };
  }

  public static GlobMatcher not(final GlobMatcher matcher) {
    return new GlobMatcher() {
      public boolean matches(Glob glob, GlobRepository repository) {
        return !matcher.matches(glob, repository);
      }
    };
  }

  public static GlobMatcher keyEquals(final Key key) {
    return new GlobMatcher() {
      public boolean matches(Glob item, GlobRepository repository) {
        return key.equals(item.getKey());
      }
    };
  }

  public static GlobMatcher contains(final Glob glob) {
    return new GlobMatcher() {
      public boolean matches(Glob item, GlobRepository repository) {
        return item.getKey().equals(glob.getKey());
      }
    };
  }

  public static GlobMatcher fieldIn(final IntegerField field, final Set<Integer> values) {
    return new GlobMatcher() {
      public boolean matches(Glob item, GlobRepository repository) {
        Integer value = item.get(field);
        return value != null && values.contains(value);
      }
    };
  }

  public static GlobMatcher fieldGreaterOrEqual(final IntegerField field, final int value) {
    return new GlobMatcher() {
      public boolean matches(Glob item, GlobRepository repository) {
        return item.get(field) >= value;
      }
    };
  }

  public static GlobMatcher fieldStrictlyGreaterThan(final IntegerField field, final int value) {
    return new GlobMatcher() {
      public boolean matches(Glob item, GlobRepository repository) {
        return item.get(field) > value;
      }
    };
  }

  public static GlobMatcher fieldStrictlyLessThan(final IntegerField field, final int value) {
    return new GlobMatcher() {
      public boolean matches(Glob item, GlobRepository repository) {
        return item.get(field) < value;
      }
    };
  }

  public static GlobMatcher fieldLessOrEqual(final IntegerField field, final int value) {
    return new GlobMatcher() {
      public boolean matches(Glob item, GlobRepository repository) {
        return item.get(field) <= value;
      }
    };
  }

  public static GlobMatcher fieldContained(final Field field, final Collection values) {
    return new GlobMatcher() {
      public boolean matches(Glob item, GlobRepository repository) {
        return values.contains(item.getValue(field));
      }
    };
  }

  private static class SingleFieldMatcher implements GlobMatcher {
    private Field field;
    private Object value;

    private SingleFieldMatcher(Field field, Object value) {
      this.field = field;
      this.value = value;
    }

    public boolean matches(Glob glob, GlobRepository repository) {
      return Utils.equal(value, glob.getValue(field));
    }

    public String toString() {
      return field + " == " + value;
    }
  }

  private static class CollectionFieldMatcher implements GlobMatcher {
    private Field field;
    private Collection values;

    private CollectionFieldMatcher(Field field, Collection values) {
      this.field = field;
      this.values = values;
    }

    public boolean matches(Glob glob, GlobRepository repository) {
      return values.contains(glob.getValue(field));
    }

    public String toString() {
      return field + " in " + values;
    }
  }
}
