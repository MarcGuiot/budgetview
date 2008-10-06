package org.globsframework.gui.editors;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.fields.*;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidFormat;

import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

public class GlobNumericEditor extends AbstractGlobTextFieldEditor<GlobNumericEditor> {
  private boolean isMinusAllowed = true;
  private boolean invertValue = false;
  private Object valueForNull = null;

  public static GlobNumericEditor init(Field field, GlobRepository repository, Directory directory) {
    return new GlobNumericEditor(field, repository, directory);
  }

  private GlobNumericEditor(Field field, GlobRepository repository, Directory directory) {
    super(field, repository, directory);
  }

  public GlobNumericEditor setMinusAllowed(boolean minusAllowed) {
    this.isMinusAllowed = minusAllowed;
    return this;
  }

  public GlobNumericEditor setValueForNull(Object value) {
    this.valueForNull = value;
    return this;
  }

  public GlobNumericEditor setInvertValue(boolean value) {
    this.invertValue = value;
    return this;
  }

  protected boolean checkValue(String str) {
    if ("-".equals(str)) {
      return isMinusAllowed;
    }
    if (!isMinusAllowed && str.startsWith("-")) {
      return false;
    }
    if ("".equals(str)) {
      return true;
    }
    try {
      convertValue(str);
      return true;
    }
    catch (Exception e) {
      return false;
    }
  }

  protected Object getValue() {
    String s = textComponent.getText();
    if (!"".equals(s.trim())) {
      s = invertIfNeeded(s);
    }
    return convertValue(s).getValue();
  }

  private String invertIfNeeded(String text) {
    if (invertValue) {
      if (text == null) {
        return "";
      }
      if (text.startsWith("-")) {
        text = text.substring(1);
      }
      else {
        text = "-" + text;
      }
    }
    return text;
  }

  private StringParserFieldVisitor convertValue(String s) {
    StringParserFieldVisitor fieldVisitor = new StringParserFieldVisitor(s, descriptionService, valueForNull);
    try {
      field.visit(fieldVisitor);
    }
    catch (Exception e) {
      throw new InvalidFormat(e);
    }
    return fieldVisitor;
  }

  protected void setValue(Object value) {
    setAdjusting(true);
    try {
      if (value == null) {
        textComponent.setText("");
        return;
      }

      StringifierFieldValueVisitor visitor = new StringifierFieldValueVisitor(descriptionService);
      field.safeVisit(visitor, value);
      String text;
      if (value.equals(valueForNull)) {
        text = visitor.getText();
      }
      else {
        text = invertIfNeeded(visitor.getText());
      }
      textComponent.setText(text);
    }
    finally {
      setAdjusting(false);
    }
  }

  protected void registerChangeListener() {
    textComponent.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        applyChanges();
      }
    });
    ((AbstractDocument)textComponent.getDocument()).setDocumentFilter(new DocumentFilter() {
      public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
        StringBuffer buffer = new StringBuffer();
        String text = textComponent.getText();
        buffer
          .append(text, 0, offset)
          .append(string);
        if (text.length() - offset > 0) {
          buffer.append(text, offset, text.length());
        }

        if (checkValue(buffer.toString())) {
          super.insertString(fb, offset, string, attr);
        }
      }

      public void replace(FilterBypass fb, int offset, int length, String replaceText, AttributeSet attrs) throws BadLocationException {
        if (isAdjusting()) {
          super.replace(fb, offset, length, replaceText, attrs);
          return;
        }
        StringBuffer buffer = new StringBuffer();
        String text = textComponent.getText();
        buffer
          .append(text, 0, offset);
        if (replaceText != null) {
          buffer.append(replaceText);
        }
        if (text.length() - offset > 0) {
          buffer.append(text, offset + length, text.length());
        }

        try {
          setAdjusting(true);
          if (Strings.isNullOrEmpty(buffer.toString())) {
            super.replace(fb, offset, length, replaceText, attrs);
            return;
          }
          if (checkValue(buffer.toString())) {
            super.replace(fb, offset, length, replaceText, attrs);
          }
        }
        finally {
          setAdjusting(false);
          applyChanges();
        }
      }

      public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
        StringBuffer buffer = new StringBuffer();
        String text = textComponent.getText();
        buffer
          .append(text, 0, offset)
          .append(text, offset + length, text.length());
        if (checkValue(buffer.toString())) {
          super.remove(fb, offset, length);
        }
      }
    });
  }

  private static class StringParserFieldVisitor implements FieldVisitor {
    private String text;
    private Object value;
    private DescriptionService descriptionService;
    private Object valueForNull;

    private StringParserFieldVisitor(String text, DescriptionService descriptionService, Object valueForNull) {
      this.text = text;
      value = text;
      this.descriptionService = descriptionService;
      this.valueForNull = valueForNull;
    }

    public void visitInteger(IntegerField field) throws Exception {
      value = valueForNull;
      if (!"".equals(text.trim())) {
        value = Integer.parseInt(text);
      }
    }

    public void visitDouble(DoubleField field) throws Exception {
      value = valueForNull;
      if (!"".equals(text.trim())) {
        value = Double.parseDouble(text.replace(',', '.'));
      }
    }

    public void visitString(StringField field) throws Exception {
      value = text;
    }

    public void visitDate(DateField field) throws Exception {
      value = valueForNull;
      if (!"".equals(text)) {
        value = descriptionService.getFormats().getDateFormat().parse(text);
      }
    }

    public void visitBoolean(BooleanField field) throws Exception {
    }

    public void visitTimeStamp(TimeStampField field) throws Exception {
      value = valueForNull;
      if (!"".equals(text)) {
        value = descriptionService.getFormats().getTimestampFormat().parse(text);
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
    private DescriptionService descriptionService;

    public StringifierFieldValueVisitor(DescriptionService descriptionService) {
      this.descriptionService = descriptionService;
    }

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
        text = descriptionService.getFormats().getDecimalFormat().format(value);
      }
    }

    public void visitString(StringField field, String value) throws Exception {
      if (value != null) {
        text = value;
      }
    }

    public void visitDate(DateField field, Date value) throws Exception {
      if (value != null) {
        text = descriptionService.getFormats().getDateFormat().format(value);
      }
    }

    public void visitBoolean(BooleanField field, Boolean value) throws Exception {
    }

    public void visitTimeStamp(TimeStampField field, Date value) throws Exception {
      if (value != null) {
        text = descriptionService.getFormats().getTimestampFormat().format(value);
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
