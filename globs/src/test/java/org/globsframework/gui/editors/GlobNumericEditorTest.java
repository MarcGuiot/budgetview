package org.globsframework.gui.editors;

import org.globsframework.gui.utils.GlobSelectionBuilder;
import org.globsframework.gui.utils.GuiComponentTestCase;
import org.globsframework.metamodel.DummyObject;
import org.globsframework.metamodel.Field;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.Glob;
import org.uispec4j.Key;
import org.uispec4j.TextBox;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Locale;

public class GlobNumericEditorTest extends GuiComponentTestCase {
  protected Glob glob;

  protected void setUp() throws Exception {
    super.setUp();
    repository =
      checker.parse("<dummyObject id='1' name='name1' date='2000/12/12' timestamp='2000/12/12 22:22:24' value='3.5'/>" +
                    "<dummyObject2 id='0'/>");
    repository.addChangeListener(changeListener);
    glob = repository.get(key1);
  }

  protected TextBox init(Field name) {
    JTextField textField =
      GlobNumericEditor.init(name, repository, directory).getComponent();
    return new TextBox(textField);
  }

  public void testUpdateWithNullValue() throws Exception {
    Glob glob1 = repository.create(DummyObject.TYPE, value(DummyObject.VALUE, 1.0));
    Glob glob2 = repository.create(DummyObject.TYPE, value(DummyObject.VALUE, null));
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

  public void testAbsoluteValueMode() throws Exception {
    JTextField textField =
      GlobNumericEditor.init(DummyObject.VALUE, repository, directory)
        .setPositiveNumbersOnly(true)
        .getComponent();
    selectionService.select(glob);
    TextBox textBox = new TextBox(textField);

    textBox.setText("");
    textBox.appendText("-");
    assertThat(textBox.textEquals(""));

    textBox.insertText("-3", 0);
    assertThat(textBox.textEquals(""));

    textBox.insertText("4", 0);
    assertThat(textBox.textEquals("4"));

    repository.update(glob.getKey(), DummyObject.VALUE, -8.8);
    selectionService.select(glob);
    assertThat(textBox.textEquals("8.8"));
  }

  public void testUpdatesAndDeletionsOnCurrentSelection() throws Exception {

    Glob glob1 = repository.create(DummyObject.TYPE, value(DummyObject.ID, 11), value(DummyObject.VALUE, 100.0));
    Glob glob2 = repository.create(DummyObject.TYPE, value(DummyObject.ID, 12), value(DummyObject.VALUE, 100.0));

    TextBox textBox = init(DummyObject.VALUE);
    selectionService.select(GlobSelectionBuilder.init().add(glob1).add(glob2).get());

    textBox.setText("1.1");
    assertEquals(1.1, glob1.get(DummyObject.VALUE));
    assertEquals(1.1, glob2.get(DummyObject.VALUE));

    repository.update(glob2.getKey(), DummyObject.VALUE, 5.0);
    assertThat(textBox.textIsEmpty());

    repository.delete(glob2);
    assertThat(textBox.textEquals("1.1"));

    textBox.setText("3.3");
    assertEquals(3.3, glob1.get(DummyObject.VALUE));

    repository.update(glob1.getKey(), DummyObject.VALUE, 4.4);
    assertThat(textBox.textEquals("4.4"));

    repository.delete(glob1);
    assertThat(textBox.textIsEmpty());
    assertFalse(textBox.isEnabled());
  }

  public void testValuesUpdatesAreTakenIntoAccount() throws Exception {
    JTextField textField =
      GlobNumericEditor.init(DummyObject.VALUE, repository, directory)
        .getComponent();
    TextBox textBox = new TextBox(textField);

    selectionService.select(glob);
    assertThat(textBox.textEquals("3.5"));

    repository.update(glob.getKey(), DummyObject.VALUE, -8.8);
    assertThat(textBox.textEquals("-8.8"));
  }

  public void testSendUpdateOnKeyPressed() throws Exception {
    JTextField textField =
      GlobNumericEditor.init(DummyObject.VALUE, repository, directory)
        .setNotifyOnKeyPressed(true)
        .getComponent();
    TextBox textBox = new TextBox(textField);

    selectionService.select(glob);
    repository.update(glob.getKey(), DummyObject.VALUE, 0.00);
    assertThat(textBox.textEquals("0"));

    textBox.setText("-8.8");
    assertThat(textBox.textEquals("-8.8"));
    changeListener.assertLastChangesEqual(
      "<update type='dummyObject' id='1' value='-8.8' _value='0.0'/>");
    textBox.setText(null);
    textBox.pressKey(Key.d5);
    changeListener.assertLastChangesEqual(
      "<update type='dummyObject' id='1' value='5.0' _value='(null)'/>");

    textBox.setText(null);
    textBox.pressKey(Key.MINUS);
    textBox.pressKey(Key.d6);
    changeListener.assertLastChangesEqual(
      "<update type='dummyObject' id='1' value='-6.0' _value='(null)'/>");

    ((JTextField)textBox.getAwtComponent()).setSelectionEnd(0);
    textBox.pressKey(Key.DELETE);
    textBox.pressKey(Key.DELETE);
    changeListener.assertLastChangesEqual(
      "<update type='dummyObject' id='1' value='(null)' _value='6.0'/>");
  }

  public void testMultiSelection() throws Exception {
    Glob glob1 = repository.create(org.globsframework.model.Key.create(DummyObject.TYPE, 2), value(DummyObject.VALUE, 1.0));
    Glob glob2 = repository.create(org.globsframework.model.Key.create(DummyObject.TYPE, 3), value(DummyObject.VALUE, 2.0));
    JTextField textField =
      GlobNumericEditor.init(DummyObject.VALUE, repository, directory)
        .setNotifyOnKeyPressed(true).getComponent();
    TextBox textBox = new TextBox(textField);

    selectionService.select(Arrays.asList(glob1, glob2), DummyObject.TYPE);
    assertThat(textBox.textEquals(""));
    selectionService.select(Arrays.asList(glob1), DummyObject.TYPE);
    assertThat(textBox.textEquals("1"));
    selectionService.select(Arrays.asList(glob2), DummyObject.TYPE);
    assertThat(textBox.textEquals("2"));
    textBox.setText("44");
    changeListener.assertLastChangesEqual(
      "<update type='dummyObject' id='3' value='44.0' _value='2.0'/>");
    selectionService.select(Arrays.asList(glob1), DummyObject.TYPE);
    assertThat(textBox.textEquals("1"));
    selectionService.select(Arrays.asList(glob2), DummyObject.TYPE);
    assertThat(textBox.textEquals("44"));
  }

  public void testSetValueForNullAndNotifyAtKeyPressed() throws Exception {
    Glob glob1 = repository.create(org.globsframework.model.Key.create(DummyObject.TYPE, 2), value(DummyObject.VALUE, 1.0));
    Glob glob2 = repository.create(org.globsframework.model.Key.create(DummyObject.TYPE, 3), value(DummyObject.VALUE, 2.0));
    JTextField textField =
      GlobNumericEditor.init(DummyObject.VALUE, repository, directory)
        .setNotifyOnKeyPressed(true)
        .setValueForNull(0.0)
        .setPositiveNumbersOnly(true)
        .getComponent();
    TextBox textBox = new TextBox(textField);

    selectionService.select(Arrays.asList(glob1, glob2), DummyObject.TYPE);
    textBox.setText("44");
    changeListener.assertLastChangesEqual(
      "<update type='dummyObject' id='2' value='44.0' _value='1.0'/>" +
      "<update type='dummyObject' id='3' value='44.0' _value='2.0'/>" +
      "");

    selectionService.select(Arrays.asList(glob1), DummyObject.TYPE);
    ((JTextField)textBox.getAwtComponent()).setSelectionEnd(0);
    textBox.pressKey(Key.DELETE);
    textBox.pressKey(Key.DELETE);
    textBox.pressKey(Key.DELETE);
    textBox.pressKey(Key.DELETE);
    textBox.pressKey(Key.DELETE);
    changeListener.assertLastChangesEqual(
      "<update type='dummyObject' id='2' value='0.0' _value='4.0'/>");
    changeListener.reset();

    selectionService.select(Arrays.asList(glob2), DummyObject.TYPE);
    assertThat(textBox.textEquals("44"));

    selectionService.select(Arrays.asList(glob1), DummyObject.TYPE);
    changeListener.assertNoChanges();
    assertThat(textBox.textEquals("0"));
  }

  public void testValidateActionIsCalledAfterLastUpdate() throws Exception {
    JTextField textField =
      GlobNumericEditor.init(DummyObject.VALUE, repository, directory)
        .setValidationAction(new AbstractAction() {
          public void actionPerformed(ActionEvent e) {
            repository.update(glob.getKey(), DummyObject.VALUE, 3.14);
          }
        })
        .setNotifyOnKeyPressed(true)
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
