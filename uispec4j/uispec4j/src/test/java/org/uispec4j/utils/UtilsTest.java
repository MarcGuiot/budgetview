package org.uispec4j.utils;

public class UtilsTest extends UnitTestCase {
  public void test() throws Exception {
    checkNormalize("text", 4, "text");
    checkNormalize("text  ", 6, "text");
    checkNormalize("text    ", 8, "text");
    checkNormalize("te", 3, "text");
    checkNormalize("", 0, "text");
    checkNormalize("", -1, "text");
  }

  public void testAssertSetEquals() throws Exception {
    final Item bag = new Item("bag");
    final Item bike = new Item("bike");
    final Item motorcycle = new Item("motorcycle");
    final Object[] collection = new Object[]{bike, motorcycle, bag};

    checkAssertionFailedError(new Functor() {
      public void run() throws Exception {
        Utils.assertSetEquals(new Object[]{bag, motorcycle}, collection, new ItemStringifier());
      }
    }, "2 elements instead of 3\n" +
        "Expected: [bag,motorcycle],\n" +
        "but was: [bike,motorcycle,bag]");
    checkAssertionFailedError(new Functor() {
      public void run() throws Exception {
        Utils.assertSetEquals(new Object[]{bag, motorcycle, bag}, collection, new ItemStringifier());
      }
    }, "Unexpected element 'bike'\n" +
        "Expected: [bag,motorcycle,bag],\n" +
        "but was: [bike,motorcycle,bag]");
    Utils.assertSetEquals(new Object[]{bag, bike, motorcycle}, collection, new ItemStringifier());
    Utils.assertSetEquals(new Object[]{bike, motorcycle, bag}, collection, new ItemStringifier());
  }

  public void testAssertEquals() throws Exception {
    final Item bag = new Item("bag");
    final Item bike = new Item("bike");
    final Item motorcycle = new Item("motorcycle");
    final Object[] collection = new Object[]{bike, motorcycle, bag};

    checkAssertionFailedError(new Functor() {
      public void run() throws Exception {
        Utils.assertEquals(new Object[]{bag, motorcycle}, collection, new ItemStringifier());
      }
    }, "2 elements instead of 3\n" +
        "Expected: [bag,motorcycle],\n" +
        "but was: [bike,motorcycle,bag]");
    checkAssertionFailedError(new Functor() {
      public void run() throws Exception {
        Utils.assertEquals(new Object[]{bag, motorcycle, bag}, collection, new ItemStringifier());
      }
    }, "Unexpected element 'bike'\n" +
        "Expected: [bag,motorcycle,bag],\n" +
        "but was: [bike,motorcycle,bag]");
    checkAssertionFailedError(new Functor() {
      public void run() throws Exception {
        Utils.assertEquals(new Object[]{bag, bike, motorcycle}, collection, new ItemStringifier());
      }
    }, "Unexpected order in the collection\n" +
        "Expected: [bag,bike,motorcycle],\n" +
        "but was: [bike,motorcycle,bag]");
    Utils.assertEquals(new Object[]{bike, motorcycle, bag}, collection, new ItemStringifier());
  }

  public void testCleanupHtml() throws Exception {
    assertEquals("Hello world !", Utils.cleanupHtml("<html><body class='myBody'> Hello world&nbsp;!</body> </html>"));
    assertEquals("Hello world!", Utils.cleanupHtml("<html><body>\n" +
                                                    "<b>Hello<br/>world</b>!\n" +
                                                    "</body></html>"));
  }

  private void checkNormalize(String result, int size, String input) {
    assertEquals(result, Utils.normalize(input, size));
  }

  private static class Item {
    private String description;

    public Item(String description) {
      this.description = description;
    }

    public String getDescription() {
      return description;
    }
  }

  private static class ItemStringifier implements Stringifier {
    public String toString(Object obj) {
      return ((Item) obj).getDescription();
    }
  }
}
