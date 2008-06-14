package org.globsframework.xml;

import junit.framework.TestCase;
import org.globsframework.metamodel.DummyObject;
import org.globsframework.metamodel.DummyObject2;
import org.globsframework.metamodel.DummyObjectWithCompositeKey;
import org.globsframework.metamodel.DummyObjectWithLinks;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobRepositoryBuilder;
import org.globsframework.model.GlobTestUtils;
import org.globsframework.model.utils.GlobBuilder;
import org.globsframework.utils.Dates;

public class XmlGlobWriterTest extends TestCase {
  private GlobRepository globRepository;

  public void testStandardCase() throws Exception {
    globRepository =
      GlobRepositoryBuilder
        .init()
        .add(GlobBuilder.init(DummyObject.TYPE)
          .set(DummyObject.ID, 1)
          .set(DummyObject.NAME, "name1")
          .get())
        .add(GlobBuilder.init(DummyObject2.TYPE)
          .set(DummyObject2.ID, 2)
          .set(DummyObject2.LABEL, "label2")
          .get())
        .get();

    GlobTestUtils.assertEquals(globRepository,
                               "<dummyObject id='1' name='name1'/>'" +
                               "<dummyObject2 id='2' label='label2'/>");
  }

  public void testWithDate() throws Exception {
    globRepository =
      GlobRepositoryBuilder
        .init()
        .add(GlobBuilder.init(DummyObject.TYPE)
          .set(DummyObject.ID, 1)
          .set(DummyObject.DATE, Dates.parse("2006/12/05"))
          .get())
        .get();

    GlobTestUtils.assertEquals(globRepository,
                               "<dummyObject id='1' date='2006/12/05'/>");
  }

  public void testLinkField() throws Exception {
    globRepository =
      GlobRepositoryBuilder
        .init()
        .add(GlobBuilder.init(DummyObject.TYPE)
          .set(DummyObject.ID, 1)
          .set(DummyObject.LINK, 2)
          .get())
        .add(GlobBuilder.init(DummyObject.TYPE)
          .set(DummyObject.ID, 2)
          .set(DummyObject.NAME, "name2")
          .get())
        .get();

    GlobTestUtils.assertEquals(globRepository,
                               "<dummyObject id='1' link='2' linkName='name2'/>'" +
                               "<dummyObject id='2' name='name2'/>");
  }

  public void testLinkWithCompositeTarget() throws Exception {
    globRepository =
      GlobRepositoryBuilder
        .init()
        .add(GlobBuilder.init(DummyObjectWithCompositeKey.TYPE)
          .set(DummyObjectWithCompositeKey.ID1, 1)
          .set(DummyObjectWithCompositeKey.ID2, 2)
          .set(DummyObjectWithCompositeKey.NAME, "foo")
          .get())
        .add(GlobBuilder.init(DummyObjectWithLinks.TYPE)
          .set(DummyObjectWithLinks.ID, 1)
          .set(DummyObjectWithLinks.TARGET_ID_1, 1)
          .set(DummyObjectWithLinks.TARGET_ID_2, 2)
          .get())
        .get();

    GlobTestUtils.assertEquals(globRepository,
                               "<dummyObjectWithCompositeKey id1='1' id2='2' name='foo'/>'" +
                               "<dummyObjectWithLinks id='1' targetId1='1' targetId2='2' compositeLink='foo'/>");
  }

  public void testLinkWithNoNamingField() throws Exception {
    globRepository =
      GlobRepositoryBuilder
        .init()
        .add(GlobBuilder.init(DummyObjectWithLinks.TYPE)
          .set(DummyObjectWithLinks.ID, 1)
          .get())
        .add(GlobBuilder.init(DummyObjectWithLinks.TYPE)
          .set(DummyObjectWithLinks.ID, 2)
          .set(DummyObjectWithLinks.SIBLING_ID, 1)
          .get())
        .get();

    GlobTestUtils.assertEquals(globRepository,
                               "<dummyObjectWithLinks id='1'/>'" +
                               "<dummyObjectWithLinks id='2' siblingId='1'/>");
  }

  public void testContainmentLink() throws Exception {
    globRepository =
      GlobRepositoryBuilder
        .init()
        .add(GlobBuilder.init(DummyObjectWithLinks.TYPE)
          .set(DummyObjectWithLinks.ID, 1)
          .set(DummyObjectWithLinks.PARENT_ID, 2)
          .get())
        .add(GlobBuilder.init(DummyObject.TYPE)
          .set(DummyObject.ID, 2)
          .get())
        .get();

    GlobTestUtils.assertEquals(globRepository, DummyObject.TYPE,
                               "<dummyObject id='2'>'" +
                               "  <dummyObjectWithLinks id='1'/>" +
                               "</dummyObject>");

  }

  public void testLinkWithNoTarget() throws Exception {
    globRepository =
      GlobRepositoryBuilder
        .init()
        .add(GlobBuilder.init(DummyObject.TYPE)
          .set(DummyObject.ID, 1)
          .set(DummyObject.LINK, 2)
          .get())
        .add(GlobBuilder.init(DummyObject.TYPE)
          .set(DummyObject.ID, 2)
          .set(DummyObject.NAME, "name2")
          .get())
        .get();

    GlobTestUtils.assertEquals(globRepository,
                               "<dummyObject id='1' link='2' linkName='name2'/>'" +
                               "<dummyObject id='2' name='name2'/>");
  }
}
