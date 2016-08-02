package com.budgetview.desktop.components;

import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class FooterBanner {

  private JPanel panel;

  private GlobRepository repository;
  private Directory directory;
  private String message;
  private boolean withHideButton;
  private Action action;
  private JButton hideButton;

  private boolean forceHidden = false;

  public FooterBanner(String message, Action action, boolean withHideButton, GlobRepository repository, Directory directory) {
    this.message = message;
    this.withHideButton = withHideButton;
    this.repository = repository;
    this.directory = directory;
    this.action = action;
    createPanel();
  }

  public void setVisible(boolean visible) {
    panel.setVisible(visible);
    GuiUtils.revalidate(panel);
  }

  public JPanel getPanel() {
    return panel;
  }

  private void createPanel() {

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/components/footerBanner.splits",
                                                      repository, directory);

    builder.add("message", new JLabel(message));
    builder.add("action", action);
    hideButton = new JButton(new HideAction());
    builder.add("hideButton", hideButton);
    hideButton.setVisible(withHideButton);
    
    panel = builder.load();
    panel.setVisible(false);
  }

  private void setMessageVisible(boolean visible) {
    panel.setVisible(!forceHidden && visible);
    GuiUtils.revalidate(panel);
  }

  private class HideAction extends AbstractAction {

    private HideAction() {
      super(Lang.get("footerBanner.hide"));
    }

    public void actionPerformed(ActionEvent actionEvent) {
      forceHidden = true;
      setMessageVisible(false);
    }
  }
}
