package org.crossbowlabs.globs.gui.actions;

import org.crossbowlabs.globs.gui.utils.GuiComponentTestCase;
import org.crossbowlabs.globs.metamodel.DummyObject;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeBuilder;
import org.crossbowlabs.globs.model.DummyChangeSetListener;
import org.crossbowlabs.globs.model.FieldValuesBuilder;
import org.crossbowlabs.globs.model.GlobRepositoryBuilder;
import org.crossbowlabs.globs.model.FieldValue;
import static org.crossbowlabs.globs.model.FieldValue.value;
import org.uispec4j.Button;
import org.uispec4j.TextBox;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;

public class CreateGlobActionTest extends GuiComponentTestCase {
  private Action action;

  protected void setUp() throws Exception {
    super.setUp();
    repository = GlobRepositoryBuilder.createEmpty();
    changeListener = new DummyChangeSetListener();
    repository.addChangeListener(changeListener);
  }

  public void testNoNameRequestedForTypesWithNoNamingField() throws Exception {
    initAction(GlobTypeBuilder.init("type").addIntegerKey("id").get());

    action.actionPerformed(null);

    changeListener.assertLastChangesEqual("<create type='type' id='0'/>");
  }

  public void testANameIsRequestedForTypesWithNamingField() throws Exception {
    initAction(DummyObject.TYPE);

    openDialog(new WindowHandler() {
      public Trigger process(Window window) throws Exception {
        Button okButton = window.getButton("OK");
        assertFalse(okButton.isEnabled());

        window.getInputTextBox().setText("name1");
        assertTrue(okButton.isEnabled());

        return okButton.triggerClick();
      }
    });

    changeListener.assertLastChangesEqual(
      "<create type='dummyObject' id='0' name='name1'/>");
  }

  public void testOkButtonIsEnabledOnlyWhenThereIsText() throws Exception {
    initAction(DummyObject.TYPE);

    openDialog(new WindowHandler() {
      public Trigger process(Window window) throws Exception {
        Button okButton = window.getButton("OK");
        assertFalse(okButton.isEnabled());

        TextBox input = window.getInputTextBox();
        assertTrue(input.textIsEmpty());
        input.appendText("name1");
        assertTrue(okButton.isEnabled());

        input.clear();
        assertFalse(okButton.isEnabled());

        input.appendText("name1");
        assertTrue(okButton.isEnabled());

        return window.getButton("OK").triggerClick();
      }
    });

    changeListener.assertLastChangesEqual(
      "<create type='dummyObject' id='0' name='name1'/>");
  }

  public void testCancel() throws Exception {
    initAction(DummyObject.TYPE);

    openDialog(new WindowHandler() {
      public Trigger process(Window window) throws Exception {
        Button okButton = window.getButton("OK");
        assertFalse(okButton.isEnabled());

        window.getInputTextBox().appendText("name1");
        assertTrue(okButton.isEnabled());

        return window.getButton("Cancel").triggerClick();
      }
    });

    changeListener.assertNoChanges();
  }

  public void testNameUnicity() throws Exception {
    initAction(DummyObject.TYPE);
    repository.create(DummyObject.TYPE, value(DummyObject.NAME, "name"));
    changeListener.reset();

    openDialog(new WindowHandler() {
      public Trigger process(Window window) throws Exception {
        TextBox input = window.getInputTextBox();
        input.insertText("name", 0);
        assertFalse(window.containsLabel("'name' already exists"));

        Button okButton = window.getButton("OK");
        okButton.click();
        assertTrue(window.containsLabel("'name' already exists"));
        assertFalse(okButton.isEnabled());

        input.appendText("1");
        assertFalse(window.containsLabel("'name' already exists"));
        assertTrue(okButton.isEnabled());
        return okButton.triggerClick();
      }
    });

    changeListener.assertLastChangesEqual(
      "<create type='dummyObject' id='1' name='name1'/>");
  }

  private void initAction(GlobType type) {
    action = new CreateGlobAction("*", type, repository, directory);
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

