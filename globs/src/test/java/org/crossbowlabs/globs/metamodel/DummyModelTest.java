package org.crossbowlabs.globs.metamodel;

import junit.framework.TestCase;
import org.crossbowlabs.globs.utils.TestUtils;

public class DummyModelTest extends TestCase {
  public void test() throws Exception {
    DummyModel.get(); // initialize all classes
    Link[] expected = {
      DummyObject.LINK,
      DummyObjectWithLinks.PARENT_LINK,
      DummyObjectWithLinkFieldId.LINK,
      DummyObjectWithRequiredLink.LINK,
    };
    TestUtils.assertContained (DummyObject.TYPE.getInboundLinks(), expected);
  }
}
