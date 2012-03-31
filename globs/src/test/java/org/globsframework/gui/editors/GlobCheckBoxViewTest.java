package org.globsframework.gui.editors;

import org.globsframework.gui.utils.GuiComponentTestCase;
import org.globsframework.metamodel.DummyObject;
import org.globsframework.model.GlobList;
import org.uispec4j.CheckBox;

import static org.globsframework.model.FieldValue.value;

public class GlobCheckBoxViewTest extends GuiComponentTestCase {

  public void test() throws Exception {
    repository =
      checker.parse("<dummyObject id='1' present='true'/>" +
                    "<dummyObject id='2' present='false'/>" +
                    "<dummyObject id='3'/>");

    GlobCheckBoxView boxView = GlobCheckBoxView.init(DummyObject.PRESENT, repository, directory);

    CheckBox checkBox = new CheckBox(boxView.getComponent());
    selectionService.select(repository.find(key1));
    assertThat(checkBox.isSelected());
    selectionService.select(repository.find(key2));
    assertFalse(checkBox.isSelected());

    checkBox.select();
    assertTrue(checkBox.isSelected());
    assertTrue(repository.find(key2).get(DummyObject.PRESENT));
  }

  public void testForceSelection() throws Exception {
    repository =
      checker.parse("<dummyObject id='1' present='true'/>" +
                    "<dummyObject id='2' present='false'/>" +
                    "<dummyObject id='3'/>");

    GlobCheckBoxView boxView = GlobCheckBoxView.init(DummyObject.PRESENT, repository, directory)
      .forceSelection(key2);

    CheckBox checkBox = new CheckBox(boxView.getComponent());
    assertFalse(checkBox.isSelected());

    selectionService.select(repository.find(key1));
    assertFalse(checkBox.isSelected());
    repository.update(key1, DummyObject.PRESENT, false);
    assertFalse(checkBox.isSelected());

    repository.update(key2, DummyObject.PRESENT, true);
    assertTrue(checkBox.isSelected());

    repository.reset(new GlobList(), DummyObject.TYPE);
    assertFalse(checkBox.isEnabled());
    assertFalse(checkBox.isSelected());

    repository.create(key2, value(DummyObject.PRESENT, true));
    assertTrue(checkBox.isSelected());
    assertTrue(checkBox.isEnabled());
  }

  public void testStartWithReset() throws Exception {
    repository =
      checker.parse("<dummyObject id='1' present='true'/>" +
                    "<dummyObject id='2' present='false'/>" +
                    "<dummyObject id='3'/>");

    GlobCheckBoxView boxView = GlobCheckBoxView.init(DummyObject.PRESENT, repository, directory);
    CheckBox checkBox = new CheckBox(boxView.getComponent());
    assertFalse(checkBox.isSelected());

    repository.reset(new GlobList(), DummyObject.TYPE);
    assertFalse(checkBox.isEnabled());
    assertFalse(checkBox.isSelected());
  }

  public void testDeleteForcedSelection() throws Exception {
    repository =
      checker.parse("<dummyObject id='1' present='true'/>" +
                    "<dummyObject id='2' present='false'/>" +
                    "<dummyObject id='3'/>");

    GlobCheckBoxView boxView =
      GlobCheckBoxView.init(DummyObject.PRESENT, repository, directory)
        .forceSelection(key2);

    CheckBox checkBox = new CheckBox(boxView.getComponent());

    repository.update(key2, DummyObject.PRESENT, true);
    assertTrue(checkBox.isSelected());

    repository.delete(key2);
    assertFalse(checkBox.isSelected());
    assertFalse(checkBox.isEnabled());

    selectionService.select(repository.get(key1));
    assertFalse(checkBox.isSelected());
    assertFalse(checkBox.isEnabled());

    repository.create(key2, value(DummyObject.PRESENT, true));
    assertTrue(checkBox.isSelected());
    assertTrue(checkBox.isEnabled());
  }
}
