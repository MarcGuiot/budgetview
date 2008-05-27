package org.crossbowlabs.globs.model.format.utils;

import java.util.ListResourceBundle;
import java.util.ResourceBundle;
import junit.framework.TestCase;
import static org.crossbowlabs.globs.metamodel.DummyObject.DATE;
import static org.crossbowlabs.globs.metamodel.DummyObject.LINK;
import static org.crossbowlabs.globs.metamodel.DummyObject.NAME;
import static org.crossbowlabs.globs.metamodel.DummyObject.PRESENT;
import static org.crossbowlabs.globs.metamodel.DummyObject.TYPE;
import static org.crossbowlabs.globs.metamodel.DummyObject.VALUE;
import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.Link;
import org.crossbowlabs.globs.metamodel.DummyObject;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobChecker;
import org.crossbowlabs.globs.model.GlobRepository;
import static org.crossbowlabs.globs.model.KeyBuilder.newKey;
import org.crossbowlabs.globs.model.format.Formats;
import org.crossbowlabs.globs.model.format.GlobStringifier;

public class DefaultDescriptionServiceTest extends TestCase {

  private DefaultDescriptionService descriptionService;
  private GlobRepository globRepository;
  private Glob emptyObject;
  private Glob dummyObject;

  protected void setUp() throws Exception {
    descriptionService = new DefaultDescriptionService();
    GlobChecker checker = new GlobChecker();
    globRepository = checker.parse("<dummyObject id='0'/>" +
                                   "<dummyObject id='1' name='name1' value='1.23456' date='2006/12/24'" +
                                   "             present='true' link='2'/>" +
                                   "<dummyObject id='2' name='name2'/>");
    emptyObject = globRepository.get(newKey(TYPE, 0));
    dummyObject = globRepository.get(newKey(TYPE, 1));
  }

  public void testFieldStringifier() throws Exception {
    checkToString(NAME, "name1");
    checkToString(VALUE, "1.23");
    checkToString(DATE, "2006-12-24");
    checkToString(PRESENT, Formats.DEFAULT_YES_VALUE);
    checkToString((Field)LINK, "name2");
  }

  public void testLinkStringifier() throws Exception {
    checkToString((Link)LINK, "name2");
  }

  public void testGlobTypeStringifier() throws Exception {
    GlobStringifier stringifier = descriptionService.getStringifier(TYPE);
    assertEquals("", stringifier.toString(null, globRepository));
    assertEquals("", stringifier.toString(emptyObject, globRepository));
    assertEquals("name1", stringifier.toString(dummyObject, globRepository));
  }

  public void testResourceBundle() throws Exception {
    ResourceBundle bundle = new ListResourceBundle() {
      protected Object[][] getContents() {
        return new Object[][]{
            {"dummyObject", "Dummy Object"},
            {"dummyObject.name", "Name of the dummy object"},
            {"dummyObject.link", "Link to a dummy object"},
        };
      }
    };
    descriptionService = new DefaultDescriptionService(Formats.DEFAULT, bundle);
    assertEquals("Dummy Object", descriptionService.getLabel(DummyObject.TYPE));
    assertEquals("Name of the dummy object", descriptionService.getLabel(DummyObject.NAME));
    assertEquals("Link to a dummy object", descriptionService.getLabel((Link)DummyObject.LINK));

    assertEquals("value", descriptionService.getLabel(DummyObject.VALUE));
  }

  private void checkToString(Field field, String result) {
    GlobStringifier stringifier = descriptionService.getStringifier(field);
    assertEquals("", stringifier.toString(null, globRepository));
    assertEquals("", stringifier.toString(emptyObject, globRepository));
    assertEquals(result, stringifier.toString(dummyObject, globRepository));
  }

  private void checkToString(Link link, String result) {
    GlobStringifier stringifier = descriptionService.getStringifier(link);
    assertEquals("", stringifier.toString(null, globRepository));
    assertEquals("", stringifier.toString(emptyObject, globRepository));
    assertEquals(result, stringifier.toString(dummyObject, globRepository));
  }
}
