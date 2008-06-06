package org.crossbowlabs.globs.gui.views;

import org.crossbowlabs.globs.metamodel.DummyObject;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.format.GlobListStringifier;
import org.uispec4j.TextBox;

import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent;

public class GlobHtmlViewTest extends GlobTextViewTestCase {
  protected TextBox init(final GlobRepository repository) {
    GlobHtmlView view =
      GlobHtmlView.init(DummyObject.TYPE, repository, directory,
                         new GlobListStringifier() {
                           public String toString(GlobList selected) {
                             return repository.getAll(DummyObject.TYPE).toString() + " / " + selected.toString();
                           }
                         });
    return new TextBox(view.getComponent());
  }

  public void testHtml() throws Exception {
    GlobHtmlView view =
      GlobHtmlView.init(DummyObject.TYPE, repository, directory,
                         new GlobListStringifier() {
                           public String toString(GlobList selected) {
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