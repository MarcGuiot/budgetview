package org.designup.picsou.gui.card.widgets;

import org.designup.picsou.gui.model.Card;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class TextNavigationWidget extends AbstractNavigationWidget {
  private JEditorPane editor = GuiUtils.createReadOnlyHtmlComponent();
  private boolean isNavigation;

  public TextNavigationWidget(Card card,
                              GlobRepository repository,
                              Directory directory) {
    super(card, repository, directory);
    this.isNavigation = true;
    editor.setText(card.getDescription());
  }

  public TextNavigationWidget(String name,
                              String title,
                              String content,
                              String icon,
                              String rolloverIcon,
                              Action action,
                              GlobRepository repository,
                              Directory directory) {
    super(name, title, icon, rolloverIcon, action, repository, directory);
    this.isNavigation = false;
    editor.setText(content);
  }

  public boolean isNavigation() {
    return isNavigation;
  }

  public JComponent getComponent() {

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/home/textNavigationWidget.splits",
                                                      repository, directory);
    builder.add("editor", editor);
    return builder.load();
  }

  protected void setText(String text) {
    editor.setText(text);
  }
}
