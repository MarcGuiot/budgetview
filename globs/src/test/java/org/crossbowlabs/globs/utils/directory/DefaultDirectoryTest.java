package org.crossbowlabs.globs.utils.directory;

import junit.framework.TestCase;

public class DefaultDirectoryTest extends TestCase {
  public void testStandardUsage() throws Exception {

    String s = "blah";

    Directory directory = new DefaultDirectory();
    directory.add(s);

    assertSame(s, directory.get(String.class));
  }

  public void testWrapper() throws Exception {

    String s1 = "blah";
    Integer i = 1;

    Directory directory = new DefaultDirectory();
    directory.add(s1);
    directory.add(i);

    Directory wrapper = new DefaultDirectory(directory);

    String s2 = "yadda";
    Boolean b = Boolean.FALSE;
    wrapper.add(s2);
    wrapper.add(b);

    assertSame(s1, directory.get(String.class));
    assertSame(s2, wrapper.get(String.class));

    assertSame(i, wrapper.get(Integer.class));
    assertSame(i, wrapper.get(Integer.class));

    assertSame(b, wrapper.get(Boolean.class));
    assertNull(directory.find(Boolean.class));
  }
}
