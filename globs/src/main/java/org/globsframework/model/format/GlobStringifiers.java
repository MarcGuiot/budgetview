package org.globsframework.model.format;

import org.globsframework.metamodel.Link;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LongField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.utils.AbstractGlobFieldStringifier;
import org.globsframework.model.format.utils.AbstractGlobStringifier;
import org.globsframework.model.format.utils.EmptyGlobStringifier;

import java.util.Comparator;

public class GlobStringifiers {
  public static final GlobStringifier EMPTY = new EmptyGlobStringifier();

  public static GlobStringifier empty(Comparator<Glob> comparator) {
    return new EmptyGlobStringifier(comparator);
  }

  public static GlobStringifier get(final StringField field) {
    return new AbstractGlobFieldStringifier<StringField, String>(field) {
      protected String valueToString(String value) {
        return value;
      }
    };
  }

  public static GlobStringifier get(final IntegerField field) {
    return new AbstractGlobFieldStringifier<IntegerField, Integer>(field) {
      protected String valueToString(Integer value) {
        return value.toString();
      }
    };
  }

  public static GlobStringifier get(final LongField field) {
    return new AbstractGlobFieldStringifier<LongField, Long>(field) {
      protected String valueToString(Long value) {
        return value.toString();
      }
    };
  }

  public static GlobStringifier target(final Link link, final GlobStringifier targetStringifier) {
    return new AbstractGlobStringifier() {
      public String toString(Glob glob, GlobRepository repository) {
        if (glob == null) {
          return "";
        }
        Glob target = repository.findLinkTarget(glob, link);
        if (target == null) {
          return "";
        }
        return targetStringifier.toString(target, repository);
      }
    };
  }
}
