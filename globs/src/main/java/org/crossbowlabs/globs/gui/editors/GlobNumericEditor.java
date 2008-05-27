package org.crossbowlabs.globs.gui.editors;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.fields.*;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.format.Formats;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.globs.utils.exceptions.InvalidFormat;

import javax.swing.*;
import java.util.Date;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class GlobNumericEditor extends AbstractGlobTextEditor<JTextField> {

  public static GlobNumericEditor init(Field field, GlobRepository globRepository, Directory directory) {
    return new GlobNumericEditor(field, globRepository, directory, new JTextField());
  }

  private GlobNumericEditor(Field field, GlobRepository globRepository,
                            Directory directory, JTextField component) {
    super(field, component, globRepository, directory);
  }

  protected Object getValue() {
    String s = textComponent.getText();
    StringParserFieldVisitor fieldVisitor = new StringParserFieldVisitor(s);
    try {
      field.visit(fieldVisitor);
    }
    catch (Exception e) {
      throw new InvalidFormat(e);
    }
    return fieldVisitor.getValue();
  }

  protected void setValue(Object value) {
    StringifierFieldValueVisitor stringifierFieldValueVisitor = new StringifierFieldValueVisitor();
    field.safeVisit(stringifierFieldValueVisitor, value);
    textComponent.setText(stringifierFieldValueVisitor.getText());
  }

  protected void registerChangeListener() {
    textComponent.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        applyChanges();
      }
    });
  }

  private static class StringParserFieldVisitor implements FieldVisitor {
    private String text;
    private Object value;

    private StringParserFieldVisitor(String text) {
      this.text = text;
      value = text;
    }

    public void visitInteger(IntegerField field) throws Exception {
      value = null;
      if (!"".equals(text.trim())) {
        value = Integer.parseInt(text);
      }
    }

    public void visitDouble(DoubleField field) throws Exception {
      value = null;
      if (!"".equals(text.trim())) {
        value = Double.parseDouble(text.replace(',', '.'));
      }
    }

    public void visitString(StringField field) throws Exception {
      value = text;
    }

    public void visitDate(DateField field) throws Exception {
      value = null;
      if (!"".equals(text)) {
        value = Formats.DEFAULT_DATE_FORMAT.parse(text);
      }
    }

    public void visitBoolean(BooleanField field) throws Exception {
    }

    public void visitTimeStamp(TimeStampField field) throws Exception {
      value = null;
      if (!"".equals(text)) {
        value = Formats.DEFAULT_TIMESTAMP_FORMAT.parse(text);
      }
    }

    public void visitBlob(BlobField field) throws Exception {
    }

    public void visitLong(LongField field) throws Exception {
    }

    public void visitLink(LinkField field) throws Exception {
    }

    public Object getValue() {
      return value;
    }
  }

  private static class StringifierFieldValueVisitor implements FieldValueVisitor {
    private String text;

    public String getText() {
      return text;
    }

    public void visitInteger(IntegerField field, Integer value) throws Exception {
      if (value != null) {
        text = value.toString();
      }
    }

    public void visitDouble(DoubleField field, Double value) throws Exception {
      if (value != null) {
        text = Formats.DEFAULT_DECIMAL_FORMAT.format(value);
      }
    }

    public void visitString(StringField field, String value) throws Exception {
      if (value != null) {
        text = value;
      }
    }

    public void visitDate(DateField field, Date value) throws Exception {
      if (value != null) {
        text = Formats.DEFAULT_DATE_FORMAT.format(value);
      }
    }

    public void visitBoolean(BooleanField field, Boolean value) throws Exception {
    }

    public void visitTimeStamp(TimeStampField field, Date value) throws Exception {
      if (value != null) {
        text = Formats.DEFAULT_TIMESTAMP_FORMAT.format(value);
      }
    }

    public void visitBlob(BlobField field, byte[] value) throws Exception {
    }

    public void visitLong(LongField field, Long value) throws Exception {
      if (value != null) {
        text = value.toString();
      }
    }

    public void visitLink(LinkField field, Integer value) throws Exception {
    }
  }
}
