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
      public String toString(GlobList selected, GlobRepository repository) {
        if (selected.isEmpty()) {
          return emptyText;
        }
        if (selected.size() == 1) {
          return singularText;
        }
        return pluralText;
      }
    };
  }

  public static GlobListStringifier sum(final DecimalFormat format, final DoubleField... fields) {
    return new GlobListStringifier() {
      public String toString(GlobList selected, GlobRepository repository) {
        if (selected.isEmpty()) {
          return "";
        }

        double total = 0;
        for (Glob glob : selected) {
          for (DoubleField field : fields) {
            total += glob.get(field);
          }
        }
        return format.format(total);
      }
    };
  }

  public static GlobListStringifier conditionnalSum(final GlobMatcher matcher,
                                                    final DecimalFormat format, final DoubleField... fields) {
    return new GlobListStringifier() {
      public String toString(GlobList selected, GlobRepository repository) {
        if (selected.isEmpty()) {
          return "";
        }

        double total = 0;
        for (Glob glob : selected) {
          if (matcher.matches(glob, repository)) {
            for (DoubleField field : fields) {
              total += glob.get(field);
            }
          }
        }
        return format.format(total);
      }
    };
  }

  public static GlobListStringifier minimum(final DoubleField field, final DecimalFormat format) {
    return new GlobListStringifier() {
      public String toString(GlobList selected, GlobRepository repository) {
        if (selected.isEmpty()) {
          return "";
        }

        double min = selected.get(0).get(field);
        for (Glob glob : selected) {
          min = Math.min(min, glob.get(field));
        }
        return format.format(min);
      }
    };
  }

  public static GlobListStringifier maximum(final DoubleField field, final DecimalFormat format) {
    return new GlobListStringifier() {
      public String toString(GlobList selected, GlobRepository repository) {
        if (selected.isEmpty()) {
          return "";
        }

        double max = selected.get(0).get(field);
        for (Glob glob : selected) {
          max = Math.max(max, glob.get(field));
        }
        return format.format(max);
      }
    };
  }

  public static GlobListStringifier average(final DoubleField field, final DecimalFormat format) {
    return new GlobListStringifier() {
      public String toString(GlobList selected, GlobRepository repository) {
        if (selected.isEmpty()) {
          return "";
        }

        double total = 0;
        for (Glob glob : selected) {
          total += glob.get(field);
        }
        return format.format(total / selected.size());
      }
    };
  }
}
