package org.globsframework.model.format;

import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatcher;

import java.text.DecimalFormat;

public class GlobListStringifiers {

  public static GlobListStringifier singularOrPlural(final String emptyText,
                                                     final String singularText,
                                                     final String pluralText) {
    return new GlobListStringifier() {
      public String toString(GlobList list, GlobRepository repository) {
        if (list.isEmpty()) {
          return emptyText;
        }
        if (list.size() == 1) {
          return singularText;
        }
        return pluralText;
      }
    };
  }

  public static GlobListStringifier valueForEmpty(final String text, final GlobListStringifier stringifier) {
    return new GlobListStringifier() {
      public String toString(GlobList list, GlobRepository repository) {
        if (list.isEmpty()) {
          return text;
        }
        return stringifier.toString(list, repository);
      }
    };
  }

  public static GlobListStringifier sum(final DoubleField field, final DecimalFormat format, boolean invert) {
    final int multiplier = invert ? -1 : 1;
    return new GlobListStringifier() {
      public String toString(GlobList list, GlobRepository repository) {
        double total = 0;
        for (Glob glob : list) {
          final Double value = glob.get(field);
          if (value != null) {
            total += value * multiplier;
          }
        }
        return format.format(total);
      }
    };
  }

  public static GlobListStringifier sum(final DecimalFormat format, boolean invert, final DoubleField... fields) {
    final int multiplier = invert ? -1 : 1;
    return new GlobListStringifier() {
      public String toString(GlobList list, GlobRepository repository) {
        if (list.isEmpty()) {
          return "";
        }

        double total = 0;
        for (Glob glob : list) {
          for (DoubleField field : fields) {
            final Double value = glob.get(field);
            if (value != null) {
              total += value * multiplier;
            }
          }
        }
        return format.format(total);
      }
    };
  }

  public static GlobListStringifier conditionalSum(final GlobMatcher matcher,
                                                   final DecimalFormat format,
                                                   final DoubleField... fields) {
    return new GlobListStringifier() {
      public String toString(GlobList list, GlobRepository repository) {
        if (list.isEmpty()) {
          return "";
        }

        double total = 0;
        for (Glob glob : list) {
          if (matcher.matches(glob, repository)) {
            for (DoubleField field : fields) {
              final Double value = glob.get(field);
              if (value != null) {
                total += glob.get(field);
              }
            }
          }
        }
        return format.format(total);
      }
    };
  }

  public static GlobListStringifier minimum(final DoubleField field, final DecimalFormat format) {
    return new GlobListStringifier() {
      public String toString(GlobList list, GlobRepository repository) {
        if (list.isEmpty()) {
          return "";
        }

        double min = list.get(0).get(field);
        for (Glob glob : list) {
          min = Math.min(min, glob.get(field));
        }
        return format.format(min);
      }
    };
  }

  public static GlobListStringifier maximum(final DoubleField field, final DecimalFormat format) {
    return new GlobListStringifier() {
      public String toString(GlobList list, GlobRepository repository) {
        if (list.isEmpty()) {
          return "";
        }

        double max = list.get(0).get(field);
        for (Glob glob : list) {
          max = Math.max(max, glob.get(field));
        }
        return format.format(max);
      }
    };
  }

  public static GlobListStringifier average(final DoubleField field, final DecimalFormat format) {
    return new GlobListStringifier() {
      public String toString(GlobList list, GlobRepository repository) {
        if (list.isEmpty()) {
          return "";
        }

        double total = 0;
        for (Glob glob : list) {
          total += glob.get(field);
        }
        return format.format(total / list.size());
      }
    };
  }
}
