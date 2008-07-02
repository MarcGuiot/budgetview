package org.globsframework.gui.views;

import org.globsframework.metamodel.DummyObject;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.utils.GlobListMatcher;
import org.uispec4j.TextBox;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class GlobHtmlViewTest extends GlobTextViewTestCase {
  protected TextBox init(final GlobRepository repository, boolean autoHide,
                         GlobListStringifier stringifier, GlobListMatcher matcher) {
    GlobHtmlView view =
      GlobHtmlView.init(DummyObject.TYPE, repository, directory, stringifier)
        .setAutoHideMatcher(matcher)
        .setAutoHideIfEmpty(autoHide);
    return new TextBox(view.getComponent());
  }

  public void testHtml() throws Exception {
    GlobHtmlView view =
      GlobHtmlView.init(DummyObject.TYPE, repository, directory,
                        new GlobListStringifier() {
                          public String toString(GlobList selected, GlobRepository repository) {
                            return "<a href='http://www.globsframework.org'>globs</a>";
                          }
                        });
    final StringBuilder builder = new StringBuilder();
    view.addHyperlinkListener(new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent e) {
        builder.append("click");
      }
    });
    TextBox textBox = new TextBox(view.getComponent());
    textBox.clickOnHyperlink("globs");
    assertEquals("click", builder.toString());
  }
}