package org.globsframework.gui.editors;

import org.globsframework.gui.utils.GuiComponentTestCase;
import org.globsframework.metamodel.DummyObject;
import org.globsframework.metamodel.Field;
import org.globsframework.model.Glob;
import org.uispec4j.TextBox;

import javax.swing.*;
import java.util.Locale;

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

  public void testDate() throws Exception {
    TextBox textBox = init(DummyObject.DATE);
    selectionService.select(glob);
    textBox.setText("2006-12-26");
    changeListener.assertLastChangesEqual(
      "<update type='dummyObject' id='1' date='2006/12/26'/>");
  }

  public void testTimeStamp() throws Exception {
    TextBox textBox = init(DummyObject.TIMESTAMP);
    selectionService.select(glob);
    textBox.setText("2006-12-26 22:30:34");
    changeListener.assertLastChangesEqual(
      "<update type='dummyObject' id='1' timestamp='2006/12/26 22:30:34'/>");
  }

  public void testDouble() throws Exception {
    TextBox textBox = init(DummyObject.VALUE);
    selectionService.select(glob);
    textBox.setText("8.8");
    changeListener.assertLastChangesEqual(
      "<update type='dummyObject' id='1' value='8.8'/>");
    changeListener.reset();
    textBox.setText("2");
    changeListener.assertLastChangesEqual(
      "<update type='dummyObject' id='1' value='2.0'/>");
  }

  public void testDoubleFr() throws Exception {
    Locale.setDefault(Locale.FRANCE);
    TextBox textBox = init(DummyObject.VALUE);
    selectionService.select(glob);
    textBox.setText("8,4");
    changeListener.assertLastChangesEqual(
      "<update type='dummyObject' id='1' value='8.4'/>");
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    Locale.setDefault(Locale.ENGLISH);
  }
}
