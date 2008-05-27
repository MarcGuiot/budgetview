package org.crossbowlabs.globs.gui.actions;

import org.crossbowlabs.globs.gui.utils.GuiComponentTestCase;
import org.crossbowlabs.globs.metamodel.DummyObject;
import org.crossbowlabs.globs.model.FieldValuesBuilder;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.FieldValue;
import static org.crossbowlabs.globs.model.FieldValue.value;
import org.uispec4j.Button;
import org.uispec4j.TextBox;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.finder.ComponentMatchers;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;
import java.util.Arrays;

public class RenameGlobActionTest extends GuiComponentTestCase {
  private Action action;
  private Glob obj1;
  private Glob obj2;

  protected void setUp() throws Exception {
    super.setUp();
    repository = checker.parse("<dummyObject id='1' name='name1'/>" +
                               "<dummyObject id='2' name='name2'/>");
    obj1 = repository.get(key1);
    obj2 = repository.get(key2);

    action = new RenameGlobAction("*", DummyObject.NAME, repository, directory);

    repository.addChangeListener(changeListener);
  }

  public void testActionIsAvailableForSingleSelectionOnly() throws Exception {
    assertFalse(action.isEnabled());

    selectionService.select(obj1);
    assertTrue(action.isEnabled());

    selectionService.select(Arrays.asList(obj1, obj2), DummyObject.TYPE);
    assertFalse(action.isEnabled());

    selectionService.select(obj1);
    assertTrue(action.isEnabled());

    selectionService.clear(DummyObject.TYPE);
    assertFalse(action.isEnabled());
  }

  public void testStandardRename() throws Exception {
    selectionService.select(obj1);

    openDialog(new WindowHandler() {
      public Trigger process(Window window) throws Exception {
        TextBox input = window.getInputTextBox();
        assertTrue(input.textEquals("name1"));

        input.setText("newName");

        Button okButton = window.getButton("OK");
        assertTrue(okButton.isEnabled());
        return okButton.triggerClick();
      }
    });

    changeListener.assertLastChangesEqual(
      "<update type='dummyObject' id='1' name='newName'/>");
  }

  public void testOkButtonIsEnabledOnlyWhenThereIsText() throws Exception {
    selectionService.select(obj1);

    openDialog(new WindowHandler() {
      public Trigger process(Window window) throws Exception {
        Button okButton = window.getButton("OK");

        TextBox input = window.getInputTextBox();
        assertTrue(okButton.isEnabled());

        input.clear();
        assertFalse(okButton.isEnabled());

        input.appendText("newName");
        assertTrue(okButton.isEnabled());

        return window.getButton("OK").triggerClick();
      }
    });

    changeListener.assertLastChangesEqual(
      "<update type='dummyObject' id='1' name='newName'/>");
  }

  public void testCancel() throws Exception {
    selectionService.select(obj1);

    openDialog(new WindowHandler() {
      public Trigger process(Window window) throws Exception {
        TextBox input = window.getInputTextBox();
        input.clear();
        input.appendText("newName");

        return window.getButton("Cancel").triggerClick();
      }
    });

    changeListener.assertNoChanges();
  }

  public void testNameUnicity() throws Exception {
    repository.create(DummyObject.TYPE, value(DummyObject.NAME, "name"));
    changeListener.reset();

    selectionService.select(obj1);

    openDialog(new WindowHandler() {
      public Trigger process(Window window) throws Exception {
        TextBox input = window.getInputTextBox();
        input.clear();
        input.appendText("name");
        assertFalse(window.containsLabel("'name' already exists"));

        Button okButton = window.getButton("OK");
        okButton.click();
        assertTrue(window.containsLabel("'name' already exists"));
        assertFalse(okButton.isEnabled());

        input.appendText("10");
        assertFalse(window.containsComponent(ComponentMatchers.displayedNameSubstring("already exists")));
        assertTrue(okButton.isEnabled());
        return okButton.triggerClick();
      }
    });

    changeListener.assertLastChangesEqual(
      "<update type='dummyObject' id='1' name='name10'/>");
  }

  public void testSpecificConditions() throws Exception {
    action = new RenameGlobAction("*", DummyObject.NAME, repository, directory) {
      protected boolean accept(Glob glob) {
        return glob.get(DummyObject.ID) == 2;
      }
    };

    selectionService.select(obj1);
    assertFalse(action.isEnabled());

    selectionService.select(obj2);
    assertTrue(action.isEnabled());
  }

  private void openDialog(WindowHandler windowHandler) {
    WindowInterceptor
      .init(new Trigger() {
        public void run() throws Exception {
          action.actionPerformed(null);
        }
      })
      .process(windowHandler)
      .run();
  }
}
