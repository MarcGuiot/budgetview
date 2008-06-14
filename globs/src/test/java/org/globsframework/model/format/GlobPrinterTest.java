package org.globsframework.model.format;

import junit.framework.TestCase;
import org.globsframework.metamodel.DummyObject;
import org.globsframework.metamodel.DummyObject2;
import org.globsframework.metamodel.GlobType;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobRepositoryBuilder;
import org.globsframework.utils.Dates;
import static org.globsframework.utils.Strings.LINE_SEPARATOR;

public class GlobPrinterTest extends TestCase {
  private GlobRepository repository;

  protected void setUp() throws Exception {
    repository = GlobRepositoryBuilder.createEmpty();
  }

  public void testStandardCase() throws Exception {
    createObj2(1, "a");
    createObj2(0, "b");
    createObj2(2, "c");
    checkOutput("===== dummyObject2 ======" + LINE_SEPARATOR +
                "| id | label |" + LINE_SEPARATOR +
                "| 0  | b     |" + LINE_SEPARATOR +
                "| 1  | a     |" + LINE_SEPARATOR +
                "| 2  | c     |" + LINE_SEPARATOR +
                LINE_SEPARATOR);
  }

  public void testTwoTypes() throws Exception {
    createObj2(1, "a");

    createObj(0, "n1", "2006/02/23", "2006/01/21 12:34:05");
    createObj(1, "n2", "2006/02/21", "2005/12/24 23:59:59");

    checkOutput("===== dummyObject ======" + LINE_SEPARATOR +
                "| id | name | value | present | date       | timestamp           | password | link |" + LINE_SEPARATOR +
                "| 0  | n1   |       |         | 2006/02/23 | 2006/01/21 12:34:05 |          |      |" + LINE_SEPARATOR +
                "| 1  | n2   |       |         | 2006/02/21 | 2005/12/24 23:59:59 |          |      |" + LINE_SEPARATOR +
                "" + LINE_SEPARATOR +
                "===== dummyObject2 ======" + LINE_SEPARATOR +
                "| id | label |" + LINE_SEPARATOR +
                "| 1  | a     |" + LINE_SEPARATOR +
                LINE_SEPARATOR);
  }

  public void testFiltering() throws Exception {
    createObj2(1, "a");
    createObj(0, "n1", "2006/02/23", "2006/01/21 12:34:05");

    checkOutput("===== dummyObject ======" + LINE_SEPARATOR +
                "| id | name | value | present | date       | timestamp           | password | link |" + LINE_SEPARATOR +
                "| 0  | n1   |       |         | 2006/02/23 | 2006/01/21 12:34:05 |          |      |" + LINE_SEPARATOR +
                "" + LINE_SEPARATOR,
                DummyObject.TYPE);

    checkOutput("===== dummyObject2 ======" + LINE_SEPARATOR +
                "| id | label |" + LINE_SEPARATOR +
                "| 1  | a     |" + LINE_SEPARATOR +
                "" + LINE_SEPARATOR,
                DummyObject2.TYPE);
  }

  public void testExcludingColumns() throws Exception {
    createObj2(1, "a");

    String actual =
      GlobPrinter.init(repository)
        .showOnly(DummyObject2.TYPE)
        .exclude(DummyObject2.ID)
        .dumpToString();

    assertEquals("===== dummyObject2 ======" + LINE_SEPARATOR +
                 "| label |" + LINE_SEPARATOR +
                 "| a     |" + LINE_SEPARATOR +
                 "" + LINE_SEPARATOR, actual);
  }

  public void testLinkFieldsUseTheTargetName() throws Exception {
    repository.create(DummyObject.TYPE,
                      value(DummyObject.ID, 1),
                      value(DummyObject.NAME, "obj1"),
                      value(DummyObject.LINK, 2));
    repository.create(DummyObject.TYPE,
                      value(DummyObject.ID, 2),
                      value(DummyObject.NAME, "obj2"));

    checkOutput("===== dummyObject ======" + LINE_SEPARATOR +
                "| id | name | value | present | date | timestamp | password | link |" + LINE_SEPARATOR +
                "| 1  | obj1 |       |         |      |           |          | obj2 |" + LINE_SEPARATOR +
                "| 2  | obj2 |       |         |      |           |          |      |" + LINE_SEPARATOR +
                "" + LINE_SEPARATOR, DummyObject.TYPE);
  }

  private void checkOutput(String expected, GlobType... types) {
    assertEquals(expected, GlobPrinter.init(repository).showOnly(types).dumpToString());
  }

  private void createObj(int id, String name, String date, String timestamp) {
    repository.create(DummyObject.TYPE,
                      value(DummyObject.ID, id),
                      value(DummyObject.NAME, name),
                      value(DummyObject.DATE, Dates.parse(date)),
                      value(DummyObject.TIMESTAMP, Dates.parseTimestamp(timestamp)));
  }

  private void createObj2(int id, String value) {
    repository.create(DummyObject2.TYPE,
                      value(DummyObject2.ID, id),
                      value(DummyObject2.LABEL, value));
  }
}
