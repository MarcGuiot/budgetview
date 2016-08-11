package org.globsframework.sqlstreams.utils;

import org.globsframework.metamodel.DummyObject;
import org.globsframework.metamodel.DummyObject2;
import org.globsframework.model.GlobChecker;
import org.globsframework.model.GlobRepository;
import org.globsframework.sqlstreams.constraints.Where;
import org.globsframework.sqlstreams.drivers.jdbc.GlobsDatabaseTestCase;
import org.globsframework.streams.xml.XmlGlobStreamReader;

public class SqlGlobRepositoryBuilderTest extends GlobsDatabaseTestCase {

  public void testNominal() throws Exception {
    populate(sqlConnection, XmlGlobStreamReader.parse(directory,
                                                      "<dummyObject id='1' name='name' value='1.1' present='true'/>" +
                                                      "<dummyObject id='4' name='word' value='3.3' present='false'/>"));
    populate(sqlConnection, XmlGlobStreamReader.parse(directory,
                                                      "<dummyObject2 id='3' label='label'/>" +
                                                      "<dummyObject2 id='4' label='label'/>"));


    SqlGlobRepositoryBuilder repositoryBuilder = SqlGlobRepositoryBuilder.init(sqlService);
    repositoryBuilder.add(Where.fieldEquals(DummyObject.NAME, "name"),
                          DummyObject.VALUE, DummyObject.DATE);
    repositoryBuilder.add(Where.and(Where.fieldEquals(DummyObject2.LABEL, "label"),
                                    Where.fieldStrictlyGreaterThan(DummyObject2.ID, 3)),
                          DummyObject2.ID, DummyObject2.LABEL);
    GlobRepository repository = repositoryBuilder.getGlobRepository();

    GlobChecker checker = new GlobChecker(globModel);
    checker.assertEquals(repository, "<dummyObject2 id='4' label='label'/>" +
                                     "<dummyObject id='1' value='1.1'/>");

  }
}
