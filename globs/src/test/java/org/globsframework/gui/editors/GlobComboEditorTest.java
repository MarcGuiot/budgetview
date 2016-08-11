package org.globsframework.gui.editors;

import org.globsframework.gui.utils.GuiComponentTestCase;
import org.globsframework.metamodel.DummyObject;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepositoryChecker;
import org.uispec4j.ComboBox;

import javax.swing.*;

import java.awt.*;

import static org.globsframework.model.FieldValue.value;

public class GlobComboEditorTest extends GuiComponentTestCase {
  protected Glob glob1;
  protected Glob glob2;
  protected Glob glob3;
  private GlobRepositoryChecker repoChecker;

  protected void setUp() throws Exception {
    super.setUp();
    repository = checker.parse(
      "<dummyObject id='1' name='name1' count='3'/>" +
      "<dummyObject id='2' name='name2' count='3'/>" +
      "<dummyObject id='3' name='name3' count='4'/>"
    );
    repository.addChangeListener(changeListener);
    repoChecker = new GlobRepositoryChecker(repository);
    glob1 = repository.get(key1);
    glob2 = repository.get(key2);
    glob3 = repository.get(key3);
  }

  private ComboBox initEditor() {
    JComboBox combo = GlobComboEditor.init(DummyObject.COUNTER, new int[]{1, 3, 5, 7}, repository, directory).getComponent();
    return new ComboBox(combo);
  }

  private ComboBox initEditorForKey1() {
    JComboBox combo = GlobComboEditor.init(DummyObject.COUNTER, new int[]{1, 3, 5, 7}, repository, directory).forceKey(key1).getComponent();
    return new ComboBox(combo);
  }

  public void testStandardUsage() throws Exception {
    ComboBox combo = initEditor();
    assertThat(combo.contentEquals("1", "3", "5", "7"));
    assertFalse(combo.isEnabled());
    assertTrue(combo.selectionEquals(""));

    selectionService.select(glob1);
    assertTrue(combo.isEnabled());
    assertTrue(combo.selectionEquals("3"));

    repository.update(key1, DummyObject.COUNTER, 5);
    assertTrue(combo.isEnabled());
    assertTrue(combo.selectionEquals("5"));

    selectionService.select(glob2);
    assertTrue(combo.isEnabled());
    assertTrue(combo.selectionEquals("3"));

    selectionService.select(new GlobList(glob1, glob2), DummyObject.TYPE);
    assertTrue(combo.isEnabled());
    assertTrue(combo.selectionEquals(""));
    combo.select("7");

    assertEquals(7, glob1.get(DummyObject.COUNTER).intValue());
    assertEquals(7, glob2.get(DummyObject.COUNTER).intValue());

    repository.delete(key2);
    assertTrue(combo.isEnabled());
    assertTrue(combo.selectionEquals("7"));
    combo.select("3");
    assertEquals(3, glob1.get(DummyObject.COUNTER).intValue());

    repository.delete(key1);
    assertFalse(combo.isEnabled());
    assertTrue(combo.selectionEquals(""));
  }

  public void testStringifier() throws Exception {

    JComboBox jComboBox =
      GlobComboEditor.init(DummyObject.COUNTER, new int[]{1, 3, 5, 7}, repository, directory)
        .setRenderer(new DefaultListCellRenderer() {
          public Component getListCellRendererComponent(JList combo, Object object, int i, boolean b, boolean b1) {
            Integer intValue = (Integer)object;
            String text = "[" + intValue + "]";
            return super.getListCellRendererComponent(combo, text, i, b, b1);
          }
        })
        .getComponent();
    ComboBox combo = new ComboBox(jComboBox);

    assertTrue(combo.contentEquals("[1]", "[3]", "[5]", "[7]"));

    selectionService.select(glob1);
    assertTrue(combo.selectionEquals("[3]"));

    repository.update(key1, DummyObject.COUNTER, 5);
    assertTrue(combo.selectionEquals("[5]"));

    combo.select("[7]");
    assertEquals(7, glob1.get(DummyObject.COUNTER).intValue());
  }

  public void testForcedUsage() throws Exception {
    ComboBox combo = initEditorForKey1();
    assertThat(combo.contentEquals("1", "3", "5", "7"));
    assertThat(combo.isEnabled());
    assertThat(combo.selectionEquals("3"));

    combo.select("5");
    repoChecker.checkFields(glob1, value(DummyObject.COUNTER, 5));

    combo.select("7");
    repoChecker.checkFields(glob1, value(DummyObject.COUNTER, 7));

    repository.update(key1, value(DummyObject.COUNTER, 1));
    assertThat(combo.selectionEquals("1"));
  }

  public void testForcedObjectDoesNotExistOnCreation() throws Exception {
    repository.delete(key1);
    ComboBox combo = initEditorForKey1();
    assertFalse(combo.isEnabled());
    assertTrue(combo.selectionEquals(null));

    repository.create(key1, value(DummyObject.COUNTER, 1));
    assertThat(combo.selectionEquals("1"));
  }

  public void testForcedObjectDeletedAndCreated() throws Exception {
    ComboBox combo = initEditorForKey1();
    assertThat(combo.selectionEquals("3"));

    repository.delete(glob1);
    assertFalse(combo.isEnabled());
    assertTrue(combo.selectionEquals(null));

    repository.create(key1, value(DummyObject.COUNTER, 7));
    assertThat(combo.selectionEquals("7"));
  }

  public void testSetName() throws Exception {
    JComboBox combo = GlobComboEditor.init(DummyObject.COUNTER, new int[]{1, 3, 5, 7}, repository, directory).forceKey(key1).setName("componentName")
      .getComponent();
    assertEquals("componentName", combo.getName());
  }
}
