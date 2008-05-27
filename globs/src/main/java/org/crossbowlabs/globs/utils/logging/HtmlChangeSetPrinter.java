package org.crossbowlabs.globs.utils.logging;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.fields.DateField;
import org.crossbowlabs.globs.metamodel.fields.LinkField;
import org.crossbowlabs.globs.metamodel.fields.StringField;
import org.crossbowlabs.globs.metamodel.fields.TimeStampField;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeUtils;
import org.crossbowlabs.globs.model.*;
import org.crossbowlabs.globs.utils.Dates;
import org.crossbowlabs.globs.utils.Strings;

class HtmlChangeSetPrinter {
  private GlobRepository repository;
  private HtmlLogger logger;

  public HtmlChangeSetPrinter(HtmlLogger logger,
                              GlobRepository repository) {
    this.logger = logger;
    this.repository = repository;
    this.repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        run(changeSet);
      }

      public void globsReset(GlobRepository repository, List<GlobType> changedTypes) {
      }
    });
  }

  public void run(ChangeSet changeSet) {
    logger.startBlock("Changes");
    logger.write("<div class='tabber'>");
    try {
      for (GlobType type : changeSet.getChangedTypes()) {
        logger.write("<div class='tabbertab' title='" + type.getName() + "'>");
        printType(type, changeSet);
        logger.write("</div>");
      }
    }
    finally {
      logger.write("</div>");
      logger.endBlock();
    }
  }

  private void printType(final GlobType type, ChangeSet changeSet) {
    final HtmlTable table = new HtmlTable(logger);
    table.writeHeader(createHeaderRow(type));
    changeSet.safeVisit(type, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        List<String> row = new ArrayList<String>();
        row.add("create");
        for (Field field : type.getFields()) {
          Object value = field.isKeyField() ? key.getValue(field) : values.getValue(field);
          row.add(getValue(field, value, key, ""));
        }
        table.writeRow(row);
      }

      public void visitUpdate(Key key, FieldValues values) throws Exception {
        List<String> row = new ArrayList<String>();
        row.add("update");
        for (Field field : type.getFields()) {
          if (field.isKeyField()) {
            row.add(getValue(field, key.getValue(field), key, ""));
          }
          else {
            if (values.contains(field)) {
              row.add(getValue(field, values.getValue(field), key, "(null)"));
            }
            else {
              row.add("");
            }
          }
        }
        table.writeRow(row);
      }

      public void visitDeletion(Key key, FieldValues values) throws Exception {
        List<String> row = new ArrayList<String>();
        row.add("delete");
        for (Field field : type.getFields()) {
          if (field.isKeyField()) {
            row.add(getValue(field, key.getValue(field), key, ""));
          }
        }
        table.writeRow(row);
      }
    });
    table.end();
  }

  private String getValue(Field field, Object value, Key key, String valueForNull) {
    if (value == null) {
      return valueForNull;
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
        Glob source = repository.find(key);
        if (source != null) {
          Glob target = repository.findLinkTarget(source, link);
          if (target != null) {
            return Strings.toString(target.get(namingField));
          }
        }
      }
    }
    return Strings.toString(value);
  }

  private List<String> createHeaderRow(GlobType type) {
    List<String> row = new ArrayList<String>();
    row.add("");
    for (Field field : type.getFields()) {
      row.add(field.getName());
    }
    return row;
  }
}
