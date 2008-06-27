package org.globsframework.gui.views;

import org.globsframework.gui.utils.GuiComponentTestCase;
import org.globsframework.metamodel.DummyObject;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobRepositoryBuilder;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.utils.GlobListMatcher;
import org.globsframework.model.utils.GlobListMatchers;
import org.uispec4j.TextBox;

import java.util.List;
import java.util.ArrayList;

public abstract class GlobTextViewTestCase extends GuiComponentTestCase {
  protected Glob glob1;
  protected Glob glob2;
  private TextBox textBox;
  protected GlobListStringifier stringifier = new GlobListStringifier() {
    public String toString(GlobList selected) {
      if (selected.isEmpty()) {
        return "";
      }
      return selected.toString();
    }
  };

  protected void setUp() throws Exception {
    super.setUp();
    repository =
      checker.parse("<dummyObject id='1'/>" +
                    "<dummyObject id='2'/>" +
                    "<dummyObject2 id='0'/>");
    glob1 = repository.get(key1);
    glob2 = repository.get(key2);
    textBox = init(repository, false);
  }

  public void testCreationWithExistingGlobs() throws Exception {
    assertTrue(textBox.textIsEmpty());
  }

  public void testLabelIsUpdatedOnSelection() throws Exception {
    selectionService.select(glob1);
    assertTrue(textBox.textEquals("[dummyObject[id=1]]"));

    selectionService.select(glob2);
    assertTrue(textBox.textEquals("[dummyObject[id=2]]"));
  }

  public void testLabelIsUpdatedOnRepositoryChanges() throws Exception {
    textBox = init(repository, false, new GlobListStringifier() {
      public String toString(GlobList selected) {
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
    assertTrue(textBox.textEquals("[1/null]"));

    repository.delete(key1);
    assertTrue(textBox.textIsEmpty());

    selectionService.select(glob2);
    assertTrue(textBox.textEquals("[2/null]"));

    repository.update(glob2.getKey(), DummyObject.NAME, "name2");
    assertTrue(textBox.textEquals("[2/name2]"));
  }

  public void testEmptyRepository() throws Exception {
    GlobRepository repository = GlobRepositoryBuilder.createEmpty();
    TextBox textBox = init(repository, false);
    assertTrue(textBox.textIsEmpty());
  }

  public void testManagesReset() throws Exception {
    selectionService.select(glob1);
    assertTrue(textBox.textEquals("[dummyObject[id=1]]"));

    repository.reset(new GlobList(glob1), DummyObject.TYPE);
    assertTrue(textBox.textIsEmpty());
  }

  public void testAutoHideIfEmpty() throws Exception {
    TextBox textBox = init(repository, true);
    assertFalse(textBox.isVisible());

    selectionService.select(glob1);
    assertTrue(textBox.isVisible());

    selectionService.clear(glob1.getType());
    assertFalse(textBox.isVisible());
  }

  public void testAutoHideWithMatcher() throws Exception {
    TextBox textBox = init(repository, new GlobListMatcher() {
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

  protected TextBox init(final GlobRepository repository, boolean autoHide) {
    return init(repository, autoHide, stringifier, GlobListMatchers.ALL);
  }

  protected TextBox init(final GlobRepository repository, GlobListMatcher matcher) {
    return init(repository, false, stringifier, matcher);
  }

  protected abstract TextBox init(GlobRepository repository, boolean autoHide,
                                  GlobListStringifier stringifier, GlobListMatcher matcher);
}
