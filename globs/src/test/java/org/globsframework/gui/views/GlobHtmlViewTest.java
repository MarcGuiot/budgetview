package org.globsframework.gui.views;

import org.globsframework.metamodel.DummyObject;
import org.globsframework.metamodel.Field;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobListStringifier;
import org.uispec4j.TextBox;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class GlobHtmlViewTest extends GlobTextViewTestCase {

  protected GlobHtmlView initView(GlobRepository repository, GlobListStringifier stringifier) {
    return GlobHtmlView.init(DummyObject.TYPE, repository, directory, stringifier);
  }

  protected GlobHtmlView initView(GlobRepository repository, Field field) {
    return GlobHtmlView.init(field, repository, directory);
  }

  protected TextComponent createComponent(AbstractGlobTextView view) {
    return new TextBoxComponent(((GlobHtmlView)view).getComponent());
  }

  public void testHtml() throws Exception {
    GlobHtmlView view =
      initView(repository, new GlobListStringifier() {
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