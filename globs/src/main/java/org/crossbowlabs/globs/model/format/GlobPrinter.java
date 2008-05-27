package org.crossbowlabs.globs.model.format;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.fields.DateField;
import org.crossbowlabs.globs.metamodel.fields.LinkField;
import org.crossbowlabs.globs.metamodel.fields.StringField;
import org.crossbowlabs.globs.metamodel.fields.TimeStampField;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeComparator;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeUtils;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.utils.Dates;
import org.crossbowlabs.globs.utils.Strings;
import org.crossbowlabs.globs.utils.TablePrinter;
import static org.crossbowlabs.globs.utils.Utils.sort;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;

public class GlobPrinter {

  public static void print(GlobRepository repository, GlobType... types) {
    GlobPrinter.init(repository).showOnly(types).run();
  }

  public static GlobPrinter init(GlobRepository repository) {
    return new GlobPrinter(repository);
  }

  private GlobRepository repository;
  private List<GlobType> types = Collections.emptyList();
  private List<Field> excludedFields = Collections.emptyList();

  private GlobPrinter(GlobRepository repository) {
    this.repository = repository;
  }

  public GlobPrinter showOnly(GlobType... types) {
    this.types = Arrays.asList(types);
    return this;
  }

  public GlobPrinter exclude(Field... fields) {
    this.excludedFields = Arrays.asList(fields);
    return this;
  }

  public void run(Writer writer) {
    if (types.isEmpty()) {
      types = sort(repository.getTypes(), GlobTypeComparator.INSTANCE);
    }
    PrintWriter printer = new PrintWriter(writer);
    for (GlobType type : types) {
      printType(type, printer);
    }
  }

  public void run() {
    run(new OutputStreamWriter(System.out));
  }

  public String dumpToString() {
    StringWriter writer = new StringWriter();
    run(writer);
    return writer.toString();
  }

  private void printType(GlobType type, PrintWriter printer) {
    printer.println("===== " + type.getName() + " ======");

    List<Object[]> rows = new ArrayList<Object[]>();
    String[] headerRow = createHeaderRow(type);
    GlobList globs = repository.getAll(type);
    for (Glob glob : globs) {
      rows.add(createRow(type, glob));
    }

    TablePrinter.print(headerRow, rows, printer);

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
        Glob target = repository.findLinkTarget(glob, link);
        if (target != null) {
          return Strings.toString(target.get(namingField));
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

