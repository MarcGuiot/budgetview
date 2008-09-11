package org.globsframework.gui.views;

import org.globsframework.gui.utils.GuiComponentTestCase;
import org.globsframework.metamodel.DummyObject;
import org.globsframework.metamodel.DummyObject2;
import org.globsframework.metamodel.Field;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobRepositoryBuilder;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.utils.ChangeSetMatchers;
import org.globsframework.model.utils.GlobListMatcher;
import org.globsframework.model.utils.GlobListMatchers;
import org.globsframework.model.utils.GlobMatchers;
import org.uispec4j.TextBox;
import org.uispec4j.assertion.Assertion;
import org.uispec4j.assertion.UISpecAssert;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.util.ArrayList;
import java.util.List;

public abstract class GlobTextViewTestCase extends GuiComponentTestCase {
  protected Glob glob1;
  protected Glob glob2;
  private TextComponent textBox;
  protected GlobListStringifier stringifier = new GlobListStringifier() {
    public String toString(GlobList selected, GlobRepository repository) {
      if (selected.isEmpty()) {
        return "";
      }
      return selected.toString();
    }
  };

  protected void setUp() throws Exception {
    super.setUp();
    repository =
      checker.parse("<dummyObject id='1' name='name1'/>" +
                    "<dummyObject id='2' name='name2'/>" +
                    "<dummyObject2 id='0'/>");
    glob1 = repository.get(key1);
    glob2 = repository.get(key2);
  }

  public void testCreationWithExistingGlobs() throws Exception {
    textBox = init(repository);
    assertTrue(textBox.textIsEmpty());
  }

  public void testLabelIsUpdatedOnSelection() throws Exception {
    textBox = init(repository);
    selectionService.select(glob1);
    assertTrue(textBox.textEquals("[dummyObject[id=1]]"));

    selectionService.select(glob2);
    assertTrue(textBox.textEquals("[dummyObject[id=2]]"));
  }

  public void testLabelIsUpdatedOnRepositoryChanges() throws Exception {
    textBox = init(repository, false, new GlobListStringifier() {
      public String toString(GlobList selected, GlobRepository repository) {
        if (selected.isEmpty()) {
          return "";
        }
        List<String> items = new ArrayList<String>();
        for (Glob glob : selected) {
          items.add(glob.get(DummyObject.ID) + "/" + glob.get(DummyObject.NAME));
        }
        return items.toString();
      }
    }, GlobListMatchers.ALL);

    selectionService.select(glob1);
    assertTrue(textBox.textEquals("[1/name1]"));

    repository.delete(key1);
    assertTrue(textBox.textIsEmpty());

    selectionService.select(glob2);
    assertTrue(textBox.textEquals("[2/name2]"));

    repository.update(glob2.getKey(), DummyObject.NAME, "newName2");
    assertTrue(textBox.textEquals("[2/newName2]"));
  }

  public void testInitWithField() throws Exception {
    textBox = init(repository, DummyObject.NAME);
    repository.update(glob1.getKey(), DummyObject.NAME, "name1");
    selectionService.select(glob1);
    assertTrue(textBox.textEquals("name1"));
  }

  public void testEmptyRepository() throws Exception {
    GlobRepository repository = GlobRepositoryBuilder.createEmpty();
    textBox = init(repository);
    assertTrue(textBox.textIsEmpty());
  }

  public void testManagesReset() throws Exception {
    textBox = init(repository);

    selectionService.select(glob1);
    assertTrue(textBox.textEquals("[dummyObject[id=1]]"));

    repository.reset(new GlobList(glob1), DummyObject.TYPE);
    assertTrue(textBox.textIsEmpty());
  }

  public void testFilter() throws Exception {
    AbstractGlobTextView view = initView(repository, DummyObject.NAME)
      .setFilter(GlobMatchers.fieldEquals(DummyObject.NAME, "name2"));
    textBox = createComponent(view);

    selectionService.select(glob1);
    assertTrue(textBox.textIsEmpty());

    selectionService.select(glob2);
    assertTrue(textBox.textEquals("name2"));

    view.setFilter(GlobMatchers.fieldEquals(DummyObject.NAME, "name1"));
    assertTrue(textBox.textIsEmpty());

    selectionService.select(glob1);
    assertTrue(textBox.textEquals("name1"));
  }

  public void testAutoHideIfEmpty() throws Exception {
    textBox = initWithAutoHide(repository);
    assertFalse(textBox.isVisible());

    selectionService.select(glob1);
    assertTrue(textBox.isVisible());

    selectionService.clear(glob1.getType());
    assertFalse(textBox.isVisible());
  }

  public void testAutoHideWithMatcher() throws Exception {
    textBox = init(repository, false, stringifier, new GlobListMatcher() {
      public boolean matches(GlobList list, GlobRepository repository) {
        return list.contains(glob1);
      }
    });
    assertFalse(textBox.isVisible());

    selectionService.select(glob1);
    assertTrue(textBox.isVisible());

    selectionService.clear(glob1.getType());
    assertFalse(textBox.isVisible());

    selectionService.select(new GlobList(glob1, glob2), DummyObject.TYPE);
    assertTrue(textBox.isVisible());

    selectionService.select(glob2);
    assertFalse(textBox.isVisible());
  }

  public void testAutoHideFilterAppliedAfterGeneralFilter() throws Exception {
    textBox = createComponent(initView(repository, DummyObject.NAME)
      .setFilter(GlobMatchers.NONE)
      .setAutoHideIfEmpty(true)
      .setAutoHideMatcher(GlobListMatchers.ALL));
    selectionService.select(glob1);
    assertFalse(textBox.isVisible());
  }

  public void testVisibilityIsPreservedIfNoAutoHideIsSet() throws Exception {
    AbstractGlobTextView view = initView(repository, DummyObject.NAME);
    textBox = createComponent(view);

    view.getComponent().setVisible(false);
    selectionService.select(glob1);
    assertFalse(textBox.isVisible());
  }

  public void testForceSelection() throws Exception {
    textBox = init(repository, glob1);
    assertTrue(textBox.textEquals("[dummyObject[id=1]]"));

    selectionService.select(glob2);
    assertTrue(textBox.textEquals("[dummyObject[id=1]]"));
  }

  public void testRemainsUnchangedWhenSelectionIsDeleted() throws Exception {
    textBox = init(repository, glob1);
    assertTrue(textBox.textEquals("[dummyObject[id=1]]"));
    repository.delete(glob1.getKey());
    assertTrue(textBox.textEquals(""));
  }

  public void testUpdateWithChangeSetMatcher() throws Exception {
    textBox = createComponent(initView(repository, new GlobListStringifier() {
      public String toString(GlobList list, GlobRepository repository) {
        int count = repository.getAll(DummyObject2.TYPE).size();
        return Integer.toString(count);
      }
    })
      .setUpdateMatcher(ChangeSetMatchers.changesForType(DummyObject2.TYPE)));

    selectionService.select(glob1);
    assertTrue(textBox.textEquals("1"));

    repository.create(DummyObject2.TYPE);
    assertTrue(textBox.textEquals("2"));
  }

  public void testUpdateAtCreationTakingLastSelection() throws Exception {
    selectionService.select(glob1);
    textBox = init(repository);
    assertTrue(textBox.textEquals("[dummyObject[id=1]]"));
  }

  protected TextComponent init(final GlobRepository repository) {
    return createComponent(initView(repository, stringifier));
  }

  protected TextComponent initWithAutoHide(final GlobRepository repository) {
    return init(repository, true, stringifier, GlobListMatchers.ALL);
  }

  protected final TextComponent init(GlobRepository repository, Glob glob) {
    return createComponent(initView(repository, stringifier).forceSelection(glob));
  }

  protected final TextComponent init(GlobRepository repository, boolean autoHide,
                                     GlobListStringifier stringifier, GlobListMatcher matcher) {
    AbstractGlobTextView view =
      initView(repository, stringifier)
        .setAutoHideMatcher(matcher)
        .setAutoHideIfEmpty(autoHide);
    return createComponent(view);
  }

  protected final TextComponent init(GlobRepository repository, Field field) {
    return createComponent(initView(repository, field));
  }

  protected abstract TextComponent createComponent(AbstractGlobTextView view);

  protected abstract AbstractGlobTextView initView(GlobRepository repository, GlobListStringifier stringifier);

  protected abstract AbstractGlobTextView initView(GlobRepository repository, Field field);

  protected interface TextComponent {

    Assertion textIsEmpty();

    Assertion textEquals(String text);

    Assertion isVisible();

    Assertion isEditable();
  }

  protected class TextBoxComponent implements TextComponent {

    private TextBox textBox;

    public TextBoxComponent(JTextComponent textComponent) {
      this.textBox = new TextBox(textComponent);
    }

    public TextBoxComponent(JLabel label) {
      this.textBox = new TextBox(label);
    }

    public Assertion textIsEmpty() {
      return textBox.textIsEmpty();
    }

    public Assertion textEquals(String text) {
      return textBox.textEquals(text);
    }

    public Assertion isVisible() {
      return textBox.isVisible();
    }

    public Assertion isEditable() {
      return textBox.isEditable();
    }
  }

  protected class ButtonComponent implements TextComponent {
    protected org.uispec4j.AbstractButton button;

    public ButtonComponent(org.uispec4j.AbstractButton button) {
      this.button = button;
    }

    public Assertion textIsEmpty() {
      return button.textEquals("");
    }

    public Assertion textEquals(String text) {
      return button.textEquals(text);
    }

    public Assertion isVisible() {
      return button.isVisible();
    }

    public Assertion isEditable() {
      return UISpecAssert.fail("Buttons are not editable");
    }
  }
}
