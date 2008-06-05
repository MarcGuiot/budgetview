package org.crossbowlabs.globs.model.format.utils;

import java.util.*;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.Link;
import org.crossbowlabs.globs.metamodel.fields.BlobField;
import org.crossbowlabs.globs.metamodel.fields.BooleanField;
import org.crossbowlabs.globs.metamodel.fields.DateField;
import org.crossbowlabs.globs.metamodel.fields.DoubleField;
import org.crossbowlabs.globs.metamodel.fields.FieldVisitor;
import org.crossbowlabs.globs.metamodel.fields.IntegerField;
import org.crossbowlabs.globs.metamodel.fields.LinkField;
import org.crossbowlabs.globs.metamodel.fields.LongField;
import org.crossbowlabs.globs.metamodel.fields.StringField;
import org.crossbowlabs.globs.metamodel.fields.TimeStampField;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeUtils;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.format.DescriptionService;
import org.crossbowlabs.globs.model.format.Formats;
import org.crossbowlabs.globs.model.format.GlobLinkStringifier;
import org.crossbowlabs.globs.model.format.GlobStringifier;
import org.crossbowlabs.globs.model.utils.GlobFieldComparator;
import org.crossbowlabs.globs.utils.Ref;

public class DefaultDescriptionService implements DescriptionService {
  private Formats formats;
  private ResourceBundle bundle;

  public DefaultDescriptionService() {
    this(Formats.DEFAULT, null);
  }

  public DefaultDescriptionService(Formats formats) {
    this(formats, null);
  }

  public DefaultDescriptionService(Formats formats, ResourceBundle bundle) {
    this.formats = formats;
    this.bundle = bundle;
  }

  public DefaultDescriptionService(Formats formats, String baseName, Locale locale, ClassLoader loader) {
    this(formats, ResourceBundle.getBundle(baseName, locale, loader));
  }

  public Formats getFormats() {
    return formats;
  }

  public String getLabel(GlobType type) {
    return getBundleValue(type.getName(), type.getName());
  }

  public String getLabel(Field field) {
    return getBundleValue(field.getGlobType().getName() + "." + field.getName(), field.getName());
  }

  public String getLabel(Link link) {
    return getBundleValue(link.getSourceType().getName() + "." + link.getName(), link.getName());
  }

  private String getBundleValue(String key, String defaultValue) {
    if (bundle != null) {
      try {
        String value = bundle.getString(key);
        if (value != null) {
          return value;
        }
      }
      catch (MissingResourceException e) {
      }
    }
    return defaultValue;
  }

  public GlobStringifier getStringifier(GlobType globType) {
    Field namingField = GlobTypeUtils.findNamingField(globType);
    if (namingField != null) {
      return getStringifier(namingField);
    }

    return new AbstractGlobStringifier() {
      public String toString(Glob glob, GlobRepository globRepository) {
        if (glob == null) {
          return "";
        }
        return glob.getKey().toString();
      }
    };
  }

  public GlobStringifier getStringifier(Field targetField) {
    final Ref<GlobStringifier> result = new Ref<GlobStringifier>();
    targetField.safeVisit(new FieldVisitor() {
      public void visitLong(LongField field) throws Exception {
        result.set(new AbstractGlobFieldStringifier<LongField, Long>(field) {
          protected String valueToString(Long value) {
            return value.toString();
          }
        });
      }

      public void visitInteger(final IntegerField field) throws Exception {
        result.set(new AbstractGlobFieldStringifier<IntegerField, Integer>(field) {
          protected String valueToString(Integer value) {
            return value.toString();
          }
        });
      }

      public void visitLink(final LinkField field) throws Exception {
        result.set(getStringifier((Link)field));
      }

      public void visitDouble(final DoubleField field) throws Exception {
        result.set(new AbstractGlobFieldStringifier<DoubleField, Double>(field) {
          protected String valueToString(Double value) {
            return formats.getDecimalFormat().format(value);
          }
        });
      }

      public void visitString(final StringField field) throws Exception {
        result.set(new AbstractGlobFieldStringifier<StringField, String>(field) {
          protected String valueToString(String value) {
            return value;
          }
        });
      }

      public void visitDate(final DateField field) throws Exception {
        result.set(new AbstractGlobFieldStringifier<DateField, Date>(field) {
          protected String valueToString(Date value) {
            return formats.getDateFormat().format(value);
          }
        });
      }

      public void visitBoolean(final BooleanField field) throws Exception {
        result.set(new AbstractGlobFieldStringifier<BooleanField, Boolean>(field) {
          protected String valueToString(Boolean value) {
            return formats.convertToString(value);
          }
        });
      }

      public void visitTimeStamp(TimeStampField field) throws Exception {
        result.set(new AbstractGlobFieldStringifier<TimeStampField, Date>(field) {
          protected String valueToString(Date value) {
            return formats.getTimestampFormat().format(value);
          }
        });
      }

      public void visitBlob(BlobField field) throws Exception {
        result.set(new AbstractGlobFieldStringifier<BlobField, byte[]>(field) {
          protected String valueToString(byte[] values) {
            return new String(values);
          }
        });
      }
    });
    return result.get();
  }

  public GlobStringifier getStringifier(Link link) {
    return new GlobLinkStringifier(link, getStringifier(link.getTargetType()));
  }

  private abstract class AbstractGlobFieldStringifier<F extends Field, T> implements GlobStringifier {
    private F field;

    public AbstractGlobFieldStringifier(F field) {
      this.field = field;
    }

    public String toString(Glob glob, GlobRepository globRepository) {
      if (glob == null) {
        return "";
      }
      T value = (T)glob.getValue(field);
      if (value == null) {
        return "";
      }
      return valueToString(value);
    }

    protected abstract String valueToString(T value);

    public Comparator<Glob> getComparator(GlobRepository globRepository) {
      return new GlobFieldComparator(field);
    }
  }
}
