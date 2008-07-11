package org.globsframework.model.utils;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.Link;
import org.globsframework.metamodel.fields.*;
import org.globsframework.metamodel.links.FieldMappingFunctor;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Ref;
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

  public static GlobMatcher fieldEquals(BooleanField field, Boolean value) {
    return fieldEqualsObject(field, value);
  }

  public static GlobMatcher fieldEquals(LongField field, Long value) {
    return fieldEqualsObject(field, value);
  }

  public static GlobMatcher fieldEqualsObject(Field field, Object value) {
    return new SingleFieldMatcher(field, value);
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
    return new GlobMatcher() {
      public boolean matches(final Glob item, GlobRepository repository) {
        final Ref<Boolean> result = new Ref<Boolean>(Boolean.TRUE);
        link.apply(new FieldMappingFunctor() {
          public void process(Field sourceField, Field targetField) {
            if (!Utils.equal(item.getValue(sourceField), target.getValue(targetField))) {
              result.set(Boolean.FALSE);
            }
          }
        });
        return result.get();
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

  public static GlobMatcher and(final GlobMatcher... matchers) {
    for (GlobMatcher matcher : matchers) {
      if ((matcher == null) || matcher.equals(NONE)) {
        return NONE;
      }
    }
    List<GlobMatcher> significantMatchers = new ArrayList<GlobMatcher>();
    for (GlobMatcher matcher : matchers) {
      if (matcher != ALL) {
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
    for (GlobMatcher matcher : matchers) {
      if ((matcher != null) && matcher.equals(ALL)) {
        return ALL;
      }
    }
    List<GlobMatcher> significantMatchers = new ArrayList<GlobMatcher>();
    for (GlobMatcher matcher : matchers) {
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
        return values.contains(item.get(field));
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
