package org.globsframework.wicket.labels;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.wicket.Component;
import org.globsframework.metamodel.DummyObject;
import org.globsframework.model.Key;
import org.globsframework.wicket.ComponentFactory;
import org.globsframework.wicket.GlobRepositoryLoader;
import org.globsframework.wicket.WebTestCase;

public class GlobLabelsTest extends WebTestCase {

  public void testStandardCase() throws Exception {

    checker.parse(repository,
                  "<dummyObject id='0' name='obj0'/>" +
                  "<dummyObject id='1' name='obj1' value='1.2' date='2006/10/31' linkName='obj0'/>");

    final Key key = Key.create(DummyObject.TYPE, 1);

    HtmlPage page = renderPage(new ComponentFactory() {
      public Component create(String componentId, GlobRepositoryLoader repositoryLoader) {
        return new LabelsPanel(componentId, key, repositoryLoader);
      }
    });

    String pageText = page.asText();

    assertEquals("name=obj1 value=1.20 date=31/10/2006 link=obj0", pageText);

    assertNoMessages(page);
  }
}
