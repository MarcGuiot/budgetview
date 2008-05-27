package org.crossbowlabs.globs.wicket.labels;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.crossbowlabs.globs.metamodel.DummyObject;
import org.crossbowlabs.globs.model.Key;
import org.crossbowlabs.globs.wicket.ComponentFactory;
import org.crossbowlabs.globs.wicket.GlobRepositoryLoader;
import org.crossbowlabs.globs.wicket.WebTestCase;
import wicket.Component;

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
