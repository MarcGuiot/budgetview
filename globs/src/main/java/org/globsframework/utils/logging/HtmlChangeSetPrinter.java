package org.globsframework.utils.logging;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.DateField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.fields.TimeStampField;
import org.globsframework.metamodel.utils.GlobTypeUtils;
import org.globsframework.model.*;
import org.globsframework.utils.Dates;
import org.globsframework.utils.Strings;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

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

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
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

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
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
