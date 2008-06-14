package org.globsframework.utils;

import junit.framework.TestCase;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

public abstract class ServicesTestCase extends TestCase {
  protected Directory directory = new DefaultDirectory();
}
