package org.globsframework.gui.views;

import org.globsframework.gui.utils.GuiComponentTestCase;
import org.globsframework.metamodel.DummyObject;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobRepositoryBuilder;
import org.globsframework.model.utils.GlobBuilder;
import org.uispec4j.TextBox;

public abstract class GlobTextViewTestCase extends GuiComponentTestCase {
  private Glob glob1;
  private Glob glob2;
  private TextBox textBox;

  protected void setUp() throws Exception {
    super.setUp();
    repository =
      checker.parse("<dummyObject id='1'/>" +
                    "<dummyObject id='2'/>" +
                    "<dummyObject2 id='0'/>");
    glob1 = repository.get(key1);
    glob2 = repository.get(key2);
    textBox = init(repository);
  }

  public void testCreationWithExistingGlobs() throws Exception {
    assertTrue(textBox.textEquals("[dummyObject[id=1], dummyObject[id=2]] / []"));
  }

  public void testLabelIsUpdatedOnSelection() throws Exception {
    selectionService.select(glob1);
    assertTrue(textBox.textEquals("[dummyObject[id=1], dummyObject[id=2]] / [dummyObject[id=1]]"));

    selectionService.select(glob2);
    assertTrue(textBox.textEquals("[dummyObject[id=1], dummyObject[id=2]] / [dummyObject[id=2]]"));
  }

  public void testLabelIsUpdatedOnRepositoryChanges() throws Exception {
    repository.delete(key1);
    assertTrue(textBox.textEquals("[dummyObject[id=2]] / []"));

    repository.create(DummyObject.TYPE, key1.toArray());
    assertTrue(textBox.textEquals("[dummyObject[id=1], dummyObject[id=2]] / []"));
  }

  public void testEmptyRepository() throws Exception {
    GlobRepository repository = GlobRepositoryBuilder.createEmpty();
    TextBox textBox = init(repository);
    assertTrue(textBox.textEquals("[] / []"));
  }

  public void testManagesReset() throws Exception {
    Glob dummyObject3 = GlobBuilder.init(DummyObject.TYPE).set(DummyObject.ID, 3).get();
    repository.reset(new GlobList(dummyObject3), DummyObject.TYPE);
    assertTrue(textBox.textEquals("[dummyObject[id=3]] / []"));
  }

  protected abstract TextBox init(GlobRepository repository);
}
