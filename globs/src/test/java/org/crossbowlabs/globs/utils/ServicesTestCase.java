package org.crossbowlabs.globs.utils;

import junit.framework.TestCase;
import org.crossbowlabs.globs.utils.directory.DefaultDirectory;
import org.crossbowlabs.globs.utils.directory.Directory;

public abstract class ServicesTestCase extends TestCase {
  protected Directory directory = new DefaultDirectory();
}
