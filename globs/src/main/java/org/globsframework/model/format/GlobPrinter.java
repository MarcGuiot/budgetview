package org.globsframework.model.format;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.DateField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.fields.TimeStampField;
import org.globsframework.metamodel.utils.GlobTypeComparator;
import org.globsframework.metamodel.utils.GlobTypeUtils;
import org.globsframework.model.FieldValues;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Dates;
import org.globsframework.utils.Strings;
import org.globsframework.utils.TablePrinter;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;

import static org.globsframework.utils.Utils.sort;

public class GlobPrinter {

  public static String toString(final FieldValues glob) {
    final StringBuilder builder = new StringBuilder();
    glob.safeApply(new FieldValues.Functor() {
      public void process(Field field, Object value) throws Exception {
        builder.append(field.getName()).append("=").append(glob.getValue(field)).append(('\n'));
      }
    });
    return builder.toString();
  }

  public static void print(GlobRepository repository, GlobType... types) {
    GlobPrinter.init(repository).showOnly(types).run();
  }

  public static void print(GlobList list) {
    GlobPrinter.init(list).run();
  }

  public static GlobPrinter init(GlobRepository repository) {
    return new GlobPrinter(repository);
  }

  public static GlobPrinter init(GlobList list) {
    return new GlobPrinter(list);
  }

  public static void print(Glob glob) {
    print(glob, new OutputStreamWriter(System.out));
  }
  
  public static void print(Glob glob, Writer writer) {
    PrintWriter printer = new PrintWriter(writer);
    printer.println("===== " + glob + " ======");

    List<Object[]> rows = new ArrayList<Object[]>();
    for (Field field : glob.getType().getFields()) {
      rows.add(new Object[]{field.getName(), glob.getValue(field)});
    }

    TablePrinter.print(new String[]{"Field", "Value"}, rows, true, printer);

    printer.println();
    printer.flush();
  }

  private GlobRepository repository;
  private Set<GlobType> types;
  private GlobList globs;
  private List<Field> excludedFields = Collections.emptyList();

  private GlobPrinter(GlobRepository repository) {
    this.repository = repository;
    this.types = repository.getTypes();
    this.globs = repository.getAll();
  }

  private GlobPrinter(GlobList list) {
    this.types = list.getTypes();
    this.globs = list;
  }

  public GlobPrinter showOnly(GlobType... shownTypes) {
    if (shownTypes.length > 0) {
      this.types = new HashSet<GlobType>(Arrays.asList(shownTypes));
    }
    return this;
  }

  public GlobPrinter exclude(Field... fields) {
    this.excludedFields = Arrays.asList(fields);
    return this;
  }

  public void run(Writer writer) {
    PrintWriter printer = new PrintWriter(writer);
    for (GlobType type : sort(types, GlobTypeComparator.INSTANCE)) {
      printType(type, globs.getAll(type), printer);
    }
  }

  public void run() {
    run(new OutputStreamWriter(System.out));
  }

  public String toString() {
    StringWriter writer = new StringWriter();
    run(writer);
    return writer.toString();
  }

  private void printType(GlobType type, GlobList globs, PrintWriter printer) {
    printer.println("===== " + type.getName() + " ======");

    List<Object[]> rows = new ArrayList<Object[]>();
    String[] headerRow = createHeaderRow(type);
    for (Glob glob : globs) {
      rows.add(createRow(type, glob));
    }

    TablePrinter.print(headerRow, rows, true, printer);

    printer.println();
    printer.flush();
  }

  private String[] createRow(GlobType type, Glob glob) {
    List<String> row = new ArrayList<String>();
    for (Field field : type.getFields()) {
      if (excludedFields.contains(field)) {
        continue;
      }
      row.add(getValue(glob, field, glob.getValue(field)));
    }
    return row.toArray(new String[row.size()]);
  }

  private String getValue(Glob glob, Field field, Object value) {
    if (value == null) {
      return "";
    }
    if ((field instanceof DateField)) {
      return Dates.toString((Date)value);
    }
    if ((field instanceof TimeStampField)) {
      return Dates.toTimestampString((Date)value);
    }
    if (field instanceof LinkField) {
      LinkField link = (LinkField)field;
      StringField namingField = GlobTypeUtils.findNamingField(link.getTargetType());
      if (namingField != null) {
        Glob target = repository != null ? repository.findLinkTarget(glob, link) : null;
        if (target != null) {
          String s = Strings.toString(target.get(namingField));
          if (Strings.isNullOrEmpty(s)) {
            return Strings.toString(value);
          }
          return "[" + value + "] " + s;
        }
      }
    }
    return Strings.toString(value);
  }

  private String[] createHeaderRow(GlobType type) {
    List<String> row = new ArrayList<String>();
    for (Field field : type.getFields()) {
      if (excludedFields.contains(field)) {
        continue;
      }
      row.add(field.getName());
    }
    return row.toArray(new String[row.size()]);
  }
}

