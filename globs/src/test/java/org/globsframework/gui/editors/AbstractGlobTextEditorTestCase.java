package org.globsframework.gui.editors;

import org.globsframework.gui.utils.GuiComponentTestCase;
import org.globsframework.gui.utils.GlobSelectionBuilder;
import org.globsframework.metamodel.DummyObject;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import static org.globsframework.model.FieldValue.value;

import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.model.repository.LocalGlobRepositoryBuilder;
import org.globsframework.model.utils.GlobBuilder;
import org.uispec4j.Key;
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
    TextBox textBox = init(DummyObject.NAME, null, true, false);
    assertTrue(textBox.textIsEmpty());
    assertFalse(textBox.isEditable());
    assertFalse(textBox.isEnabled());

    selectionService.select(glob1);
    assertTrue(textBox.isEditable());
    assertTrue(textBox.isEnabled());
    assertTrue(textBox.textEquals("name1"));
    enterTextAndValidate(textBox, "newName1");
    changeListener.assertLastChangesEqual(
      "<update type='dummyObject' id='1' name='newName1' _name='name1'/>");
    assertEquals("newName1", glob1.get(DummyObject.NAME));

    selectionService.select(glob2);
    enterTextAndValidate(textBox, "newName2");
    changeListener.assertLastChangesEqual(
      "<update type='dummyObject' id='2' name='newName2' _name='name2'/>");

    selectionService.select(Collections.EMPTY_LIST, DummyObject.TYPE);
    assertTrue(textBox.textIsEmpty());
    assertFalse(textBox.isEditable());
    assertFalse(textBox.isEnabled());
  }

  public void testMultiSelection() throws Exception {
    TextBox textBox = init(DummyObject.NAME, null, true, false);

    selectionService.select(Arrays.asList(glob1, glob2), DummyObject.TYPE);
    assertTrue(textBox.isEditable());
    assertTrue(textBox.isEnabled());
    assertTrue(textBox.textEquals(""));

    enterTextAndValidate(textBox, "newName");
    changeListener.assertLastChangesEqual(
      "<update type='dummyObject' id='1' name='newName' _name='name1'/>" +
      "<update type='dummyObject' id='2' name='newName' _name='name2'/>"
    );

    selectionService.select(Collections.EMPTY_LIST, DummyObject.TYPE);
    assertTrue(textBox.textIsEmpty());

    selectionService.select(Arrays.asList(glob1, glob2), DummyObject.TYPE);
    assertTrue(textBox.textEquals("newName"));

    enterTextAndValidate(textBox, "anotherName");
    changeListener.assertLastChangesEqual(
      "<update type='dummyObject' id='1' name='anotherName' _name='newName'/>" +
      "<update type='dummyObject' id='2' name='anotherName' _name='newName'/>"
    );
  }

  public void testFocusLostAppliesChanges() throws Exception {
    TextBox textBox = init(DummyObject.NAME, null, true, false);
    selectionService.select(glob1);
    assertTrue(textBox.textEquals("name1"));
    textBox.clear();
    assertTrue(textBox.textEquals(""));

    changeListener.reset();
    textBox.appendText("newName");
    changeListener.assertNoChanges();

    simulateFocusLost(textBox);
    changeListener.assertLastChangesEqual(
      "<update type='dummyObject' id='1' name='newName' _name='name1'/>"
    );
  }

  public void testForceSelectionAfterInit() throws Exception {
    TextBox textBox = init(DummyObject.NAME, "...", false, false);
    forceEdition(glob1.getKey());
    selectionService.select(Arrays.asList(glob2), DummyObject.TYPE);
    assertTrue(textBox.textEquals("name1"));
  }

  public void testMultiSelectionWithDifferentValues() throws Exception {
    TextBox textBox = init(DummyObject.NAME, "...", false, false);
    selectionService.select(Arrays.asList(glob1, glob2), DummyObject.TYPE);
    assertFalse(textBox.isEditable());
    assertTrue(textBox.isEnabled());
    assertTrue(textBox.textEquals("..."));
    simulateFocusLost(textBox);
    changeListener.assertNoChanges();
  }

  public void testChangesAreSentOnKeyPressed() throws Exception {
    TextBox textBox = init(DummyObject.NAME, null, true, true);
    selectionService.select(glob1);
    textBox.setText("");
    textBox.insertText("AA", 0);
    changeListener.assertLastChangesEqual(
      "<update type='dummyObject' id='1' name='AA' _name=''/>");
    textBox.pressKey(Key.shift(Key.A));
    changeListener.assertLastChangesEqual(
      "<update type='dummyObject' id='1' name='AAA' _name='AA'/>");
    textBox.pressKey(Key.shift(Key.B));
    changeListener.assertLastChangesEqual(
      "<update type='dummyObject' id='1' name='AAAB' _name='AAA'/>");
  }

  public void testSelectionsDoNotSendChanges() throws Exception {
    TextBox textBox = init(DummyObject.NAME, null, true, true);
    selectionService.select(glob1);
    textBox.setText("");
    textBox.insertText("AA", 0);
    changeListener.assertLastChangesEqual(
      "<update type='dummyObject' id='1' name='AA' _name=''/>");
    changeListener.reset();
    selectionService.select(glob2);
    changeListener.assertNoChanges();
  }

  public void testForceSelectionAndCreate() throws Exception {
    TextBox textBox = init(DummyObject.NAME, "...", false, false);
    org.globsframework.model.Key key = org.globsframework.model.Key.create(DummyObject.TYPE, 10);
    forceEdition(key);
    assertFalse(textBox.isEnabled());
    repository.create(key, value(DummyObject.NAME, "name 100"));
    assertTrue(textBox.textEquals("name 100"));
  }

  public void testForceSelectionAndReset() throws Exception {
    TextBox textBox = init(DummyObject.NAME, "...", false, false);
    org.globsframework.model.Key key = org.globsframework.model.Key.create(DummyObject.TYPE, 10);
    forceEdition(key);
    assertFalse(textBox.isEnabled());
    Glob glob = GlobBuilder.init(key, value(DummyObject.NAME, "name 100")).get();
    repository.reset(new GlobList(glob), DummyObject.TYPE);
    assertTrue(textBox.textEquals("name 100"));
  }

  public void testUpdatesAndDeletionsOnCurrentSelection() throws Exception {
    TextBox textBox = init(DummyObject.NAME, "...", true, true);
    selectionService.select(GlobSelectionBuilder.init().add(glob1).add(glob2).get());

    textBox.setText("aa");
    assertEquals("aa", glob1.get(DummyObject.NAME));
    assertEquals("aa", glob2.get(DummyObject.NAME));

    repository.update(glob2.getKey(), DummyObject.NAME, "cc");
    assertThat(textBox.textEquals("..."));

    repository.delete(glob2.getKey());
    assertThat(textBox.textEquals("aa"));

    textBox.setText("bb");
    assertEquals("bb", glob1.get(DummyObject.NAME));

    repository.update(glob1.getKey(), DummyObject.NAME, "dd");
    assertThat(textBox.textEquals("dd"));
    
    repository.delete(glob1.getKey());
    assertThat(textBox.textEquals(""));
    assertFalse(textBox.isEnabled());
  }
  
  public void testRoolbackOfLocalRepository() throws Exception {
    LocalGlobRepository localGlobRepository = LocalGlobRepositoryBuilder.init(repository).copy(DummyObject.TYPE).get();
    repository = localGlobRepository;
    TextBox textBox = init(DummyObject.NAME, "...", true, true);
    glob1 = localGlobRepository.get(glob1.getKey());
    selectionService.select(GlobSelectionBuilder.init().add(glob1).get());

    textBox.setText("BBB");
    assertEquals("BBB", glob1.get(DummyObject.NAME));
    localGlobRepository.rollback();
    assertThat(textBox.textEquals("name1"));
  }

  protected abstract TextBox init(StringField field, String defaultValueForMultivalue, boolean isEditable, boolean sendAtKeyPressed);

  abstract void forceEdition(org.globsframework.model.Key key);

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
