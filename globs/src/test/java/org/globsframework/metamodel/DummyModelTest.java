package org.globsframework.metamodel;

import junit.framework.TestCase;
import org.globsframework.utils.TestUtils;

public class DummyModelTest extends TestCase {
  public void test() throws Exception {
    DummyModel.get(); // initialize all classes
    Link[] expected = {
      DummyObject.LINK,
      DummyObjectWithLinks.PARENT_LINK,
      DummyObjectWithLinkFieldId.LINK,
      DummyObjectWithRequiredLink.LINK,
    };
    TestUtils.assertContained(DummyObject.TYPE.getInboundLinks(), expected);
  }
}
