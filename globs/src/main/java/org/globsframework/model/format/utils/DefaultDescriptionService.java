package org.globsframework.model.format.utils;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.Link;
import org.globsframework.metamodel.fields.*;
import org.globsframework.metamodel.utils.GlobTypeUtils;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobList;
import org.globsframework.model.format.*;
import org.globsframework.utils.Ref;
import org.globsframework.utils.Strings;
import org.globsframework.utils.Utils;

import java.util.Date;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

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

  public GlobListStringifier getListStringifier(GlobType type) {
    final GlobStringifier stringifier = getStringifier(type);
    return new CompositeGlobListStringifier(stringifier);
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

  public GlobListStringifier getListStringifier(Field field) {
    GlobStringifier stringifier = getStringifier(field);
    return new CompositeGlobListStringifier(stringifier);
  }

  public GlobStringifier getStringifier(Link link) {
    return new GlobLinkStringifier(link, getStringifier(link.getTargetType()));
  }

  public GlobListStringifier getListStringifier(Link link) {
    GlobStringifier stringifier = getStringifier(link);
    return new CompositeGlobListStringifier(stringifier);
  }

  public GlobStringifier getStringifier(LinkField link) {
    return getStringifier((Link)link);
  }

  public GlobListStringifier getListStringifier(LinkField link) {
    return getListStringifier((Link)link);
  }

  private static class CompositeGlobListStringifier implements GlobListStringifier {
    private final GlobStringifier stringifier;

    public CompositeGlobListStringifier(GlobStringifier stringifier) {
      this.stringifier = stringifier;
    }

    public String toString(GlobList list, GlobRepository repository) {
      if (list.isEmpty()) {
        return "";
      }
      String current = stringifier.toString(list.get(0), repository);
      for (Glob glob : list) {
        String text = stringifier.toString(glob, repository);
        if (!Utils.equal(current, text)) {
          return "...";
        }
      }
      return current;
    }
  }
}
