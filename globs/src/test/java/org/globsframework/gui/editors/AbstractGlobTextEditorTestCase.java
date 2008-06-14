package org.globsframework.gui.editors;

import org.globsframework.gui.utils.GuiComponentTestCase;
import org.globsframework.metamodel.DummyObject;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.model.Glob;
import org.uispec4j.TextBox;

import javax.swing.text.JTextComponent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Arrays;
import java.util.Collections;

public abstract class AbstractGlobTextEditorTestCase extends GuiComponentTestCase {
  protected Glob glob1;
  protected Glob glob2;

  protected void setUp() throws Exception {
    super.setUp();
    repository =
      checker.parse("<dummyObject id='1' name='name1'/>" +
                    "<dummyObject id='2' name='name2'/>" +
                    "<dummyObject2 id='0'/>");
    repository.addChangeListener(changeListener);
    glob1 = repository.get(key1);
    glob2 = repository.get(key2);
  }

  public void testSingleSelection() throws Exception {
    TextBox textBox = init(DummyObject.NAME);
    assertTrue(textBox.textIsEmpty());
    assertFalse(textBox.isEditable());
    assertFalse(textBox.isEnabled());

    selectionService.select(glob1);
    assertTrue(textBox.isEditable());
    assertTrue(textBox.isEnabled());
    assertTrue(textBox.textEquals("name1"));
    enterTextAndValidate(textBox, "newName1");
    changeListener.assertLastChangesEqual(
      "<update type='dummyObject' id='1' name='newName1'/>");
    assertEquals("newName1", glob1.get(DummyObject.NAME));

    selectionService.select(glob2);
    enterTextAndValidate(textBox, "newName2");
    changeListener.assertLastChangesEqual(
      "<update type='dummyObject' id='2' name='newName2'/>");

    selectionService.select(Collections.EMPTY_LIST, DummyObject.TYPE);
    assertTrue(textBox.textIsEmpty());
    assertFalse(textBox.isEditable());
    assertFalse(textBox.isEnabled());
  }

  public void testMultiSelection() throws Exception {
    TextBox textBox = init(DummyObject.NAME);

    selectionService.select(Arrays.asList(glob1, glob2), DummyObject.TYPE);
    assertTrue(textBox.isEditable());
    assertTrue(textBox.isEnabled());
    assertTrue(textBox.textEquals(""));

    enterTextAndValidate(textBox, "newName");
    changeListener.assertLastChangesEqual(
      "<update type='dummyObject' id='1' name='newName'/>" +
      "<update type='dummyObject' id='2' name='newName'/>"
    );

    selectionService.select(Collections.EMPTY_LIST, DummyObject.TYPE);
    assertTrue(textBox.textIsEmpty());

    selectionService.select(Arrays.asList(glob1, glob2), DummyObject.TYPE);
    assertTrue(textBox.textEquals("newName"));

    enterTextAndValidate(textBox, "anotherName");
    changeListener.assertLastChangesEqual(
      "<update type='dummyObject' id='1' name='anotherName'/>" +
      "<update type='dummyObject' id='2' name='anotherName'/>"
    );
  }

  public void testFocusLostAppliesChanges() throws Exception {
    TextBox textBox = init(DummyObject.NAME);
    selectionService.select(glob1);
    assertTrue(textBox.textEquals("name1"));
    textBox.clear();
    assertTrue(textBox.textEquals(""));

    changeListener.reset();
    textBox.appendText("blah");
    changeListener.assertNoChanges();

    simulateFocusLost(textBox);
    changeListener.assertLastChangesEqual(
      "<update type='dummyObject' id='1' name='blah'/>"
    );
  }

  protected abstract TextBox init(StringField field);

  protected void enterTextAndValidate(TextBox textBox, String text) {
    textBox.setText(text);
  }

  protected void simulateFocusLost(TextBox textBox) {
    JTextComponent textComponent = (JTextComponent)textBox.getAwtComponent();
    FocusListener[] focusListeners = textComponent.getFocusListeners();
    for (FocusListener focusListener : focusListeners) {
      FocusEvent event = new FocusEvent(textComponent, 1);
      focusListener.focusLost(event);
    }
  }
}
