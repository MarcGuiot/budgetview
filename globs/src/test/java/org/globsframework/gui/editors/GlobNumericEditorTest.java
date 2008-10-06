package org.globsframework.gui.editors;

import org.globsframework.gui.utils.GuiComponentTestCase;
import org.globsframework.metamodel.DummyObject;
import org.globsframework.metamodel.Field;
import org.globsframework.model.FieldValue;
import org.globsframework.model.Glob;
import org.uispec4j.Key;
import org.uispec4j.TextBox;

import javax.swing.*;
import java.util.Locale;
import java.awt.event.ActionEvent;

public class GlobNumericEditorTest extends GuiComponentTestCase {
  protected Glob glob;

  protected TextBox init(Field name) {
    JTextField textField =
      GlobNumericEditor.init(name, repository, directory).getComponent();
    return new TextBox(textField);
  }

  protected void setUp() throws Exception {
    super.setUp();
    repository =
      checker.parse("<dummyObject id='1' name='name1' date='2000/12/12' timestamp='2000/12/12 22:22:24' value='3.5'/>" +
                    "<dummyObject2 id='0'/>");
    repository.addChangeListener(changeListener);
    glob = repository.get(key1);
  }

  public void testUpdateWithNullValue() throws Exception {
    Glob glob1 = repository.create(DummyObject.TYPE, FieldValue.value(DummyObject.VALUE, 1.0));
    Glob glob2 = repository.create(DummyObject.TYPE, FieldValue.value(DummyObject.VALUE, null));
    TextBox textBox = init(DummyObject.VALUE);

    selectionService.select(glob1);
    assertThat(textBox.textEquals("1"));

    selectionService.select(glob2);
    assertThat(textBox.textIsEmpty());
  }

  public void testDate() throws Exception {
    TextBox textBox = init(DummyObject.DATE);
    selectionService.select(glob);
    textBox.setText("2006/12/26");
    changeListener.assertLastChangesEqual(
      "<update type='dummyObject' id='1' date='2006/12/26' _date='2000/12/12'/>");
  }

  public void testTimeStamp() throws Exception {
    TextBox textBox = init(DummyObject.TIMESTAMP);
    selectionService.select(glob);
    textBox.setText("2006/12/26 22:30:34");
    changeListener.assertLastChangesEqual(
      "<update type='dummyObject' id='1' timestamp='2006/12/26 22:30:34' _timestamp='2000/12/12 22:22:24'/>");
  }

  public void testDouble() throws Exception {
    TextBox textBox = init(DummyObject.VALUE);
    selectionService.select(glob);
    textBox.setText("8.8");
    changeListener.assertLastChangesEqual(
      "<update type='dummyObject' id='1' value='8.8' _value='3.5'/>");
    changeListener.reset();
    textBox.setText("2");
    changeListener.assertLastChangesEqual(
      "<update type='dummyObject' id='1' value='2.0' _value='8.8'/>");
  }

  public void testDoubleFr() throws Exception {
    Locale.setDefault(Locale.FRANCE);
    TextBox textBox = init(DummyObject.VALUE);
    selectionService.select(glob);
    textBox.setText("8,4");
    changeListener.assertLastChangesEqual(
      "<update type='dummyObject' id='1' value='8.4' _value='3.5'/>");
  }

  public void testAcceptsOnlyNumbers() throws Exception {
    Locale.setDefault(Locale.FRANCE);
    TextBox textBox = init(DummyObject.VALUE);
    selectionService.select(glob);
    textBox.setText("");
    textBox.insertText("8,4", 0);
    textBox.insertText("Er", 2);
    textBox.insertText("3", 1);
    assertThat(textBox.textEquals("83,4"));
    textBox.insertText(",", 0);
    assertThat(textBox.textEquals("83,4"));
  }

  public void testMinusAllowed() throws Exception {
    Locale.setDefault(Locale.FRANCE);
    TextBox textBox = init(DummyObject.VALUE);
    selectionService.select(glob);
    textBox.setText("");
    textBox.insertText("-", 0);
    textBox.insertText("83,4", 1);
    assertThat(textBox.textEquals("-83,4"));
  }

  public void testMinusNotAllowed() throws Exception {
    JTextField textField =
      GlobNumericEditor.init(DummyObject.LINK, repository, directory).setMinusAllowed(false).getComponent();
    selectionService.select(glob);
    TextBox textBox = new TextBox(textField);

    textBox.setText("");
    textBox.insertText("-3", 0);
    assertThat(textBox.textEquals(""));

    textBox.insertText("4", 0);
    assertThat(textBox.textEquals("4"));
  }

  public void testInvert() throws Exception {
    JTextField textField =
      GlobNumericEditor.init(DummyObject.VALUE, repository, directory)
        .setInvertValue(true).getComponent();
    TextBox textBox = new TextBox(textField);
    selectionService.select(glob);
    textBox.setText("8.8");
    changeListener.assertLastChangesEqual(
      "<update type='dummyObject' id='1' value='-8.8' _value='3.5'/>");
    textBox.setText("-3");
    changeListener.assertLastChangesEqual(
      "<update type='dummyObject' id='1' value='3.0' _value='-8.8'/>");
  }

  public void testInvertAndMinusNotAllowed() throws Exception {
    JTextField textField =
      GlobNumericEditor.init(DummyObject.VALUE, repository, directory)
        .setMinusAllowed(false)
        .setInvertValue(true)
        .getComponent();
    TextBox textBox = new TextBox(textField);
    repository.update(glob.getKey(), DummyObject.VALUE, -8.8);
    selectionService.select(glob);
    assertThat(textBox.textEquals("8.8"));
  }

  public void testSendUpdateAtKeyPressed() throws Exception {
    JTextField textField =
      GlobNumericEditor.init(DummyObject.VALUE, repository, directory)
        .setMinusAllowed(true)
        .setNotifyAtKeyPressed(true)
        .getComponent();
    TextBox textBox = new TextBox(textField);
    repository.update(glob.getKey(), DummyObject.VALUE, 0.);
    selectionService.select(glob);
    textBox.setText("-8.8");
    assertThat(textBox.textEquals("-8.8"));
    changeListener.assertLastChangesEqual(
      "<update type='dummyObject' id='1' value='-8.8' _value='0.0'/>");
    textBox.setText(null);
    textBox.pressKey(Key.d5);
    changeListener.assertLastChangesEqual(
      "<update type='dummyObject' id='1' value='5.0' _value='0.0'/>");

    textBox.setText(null);
    textBox.pressKey(Key.MINUS);
    textBox.pressKey(Key.d6);
    changeListener.assertLastChangesEqual(
      "<update type='dummyObject' id='1' value='-6.0' _value='0.0'/>");
  }

  public void testValidateActionIsCalledAfterLastUpdate() throws Exception {
    JTextField textField =
      GlobNumericEditor.init(DummyObject.VALUE, repository, directory)
        .setValidationAction(new AbstractAction() {
          public void actionPerformed(ActionEvent e) {
            repository.update(glob.getKey(), DummyObject.VALUE, 3.14);
          }
        })
        .setNotifyAtKeyPressed(true)
        .getComponent();
    TextBox textBox = new TextBox(textField);

    selectionService.select(glob);
    textBox.setText("8.8");
    changeListener.assertLastChangesEqual(
      "<update type='dummyObject' id='1' value='3.14' _value='8.8'/>");

  }

  protected void tearDown() throws Exception {
    super.tearDown();
    Locale.setDefault(Locale.ENGLISH);
  }
}
