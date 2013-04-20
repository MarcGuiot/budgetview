package org.designup.picsou.gui.mobile.utils;

import org.apache.wicket.util.string.Strings;
import org.designup.picsou.gui.components.tips.ErrorTip;
import org.designup.picsou.gui.components.tips.TipPosition;
import org.designup.picsou.model.UserPreferences;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.ComponentHolder;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.editors.GlobTextEditor;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class PasswordEditionPanel {

  private final GlobRepository repository;
  private final Directory directory;
  private JPanel panel;
  private CardHandler cards;
  private String lastPassword;
  private GlobTextEditor passwordField;

  public PasswordEditionPanel(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
  }

  public JPanel getPanel() {
    if (panel == null) {
      createPanel();
    }
    return panel;
  }

  public boolean check() {
    if (Strings.isEmpty(passwordField.getComponent().getText())) {
      ErrorTip.show(passwordField.getComponent(), Lang.get("mobile.password.empty"), directory, TipPosition.TOP_LEFT);
      return false;
    }
    return true;
  }

  private void createPanel() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/mobile/utils/passwordEditionPanel.splits",
                                                      repository, directory);

    cards = builder.addCardHandler("passwordCards");

    builder.addLabel("passwordLabel", UserPreferences.PASSWORD_FOR_MOBILE);
    builder.add("changePassword", new ChangePasswordAction());

    passwordField = builder.addEditor("passwordField", UserPreferences.PASSWORD_FOR_MOBILE);
    builder.add("applyPasswordEdit", new ApplyPasswordEditAction());
    builder.add("cancelPasswordEdit", new CancelPasswordEditAction());

    cards.show("readonly");

    panel = builder.load();
  }

  protected class ChangePasswordAction extends AbstractAction{
    public ChangePasswordAction() {
      super(Lang.get("mobile.user.change.password"));
    }

    public void actionPerformed(ActionEvent e) {
      lastPassword = repository.get(UserPreferences.KEY).get(UserPreferences.PASSWORD_FOR_MOBILE);
      cards.show("edit");
      GuiUtils.selectAndRequestFocus(passwordField.getComponent());
    }
  }

  protected class ApplyPasswordEditAction extends AbstractAction{
    public ApplyPasswordEditAction() {
      super(Lang.get("mobile.user.apply.password"));
    }

    public void actionPerformed(ActionEvent e) {
      if (!check()) {
        return;
      }
      lastPassword = repository.get(UserPreferences.KEY).get(UserPreferences.PASSWORD_FOR_MOBILE);
      cards.show("readonly");
    }
  }

  protected class CancelPasswordEditAction extends AbstractAction{
    public CancelPasswordEditAction() {
      super(Lang.get("cancel"));
    }

    public void actionPerformed(ActionEvent e) {
      repository.update(UserPreferences.KEY, UserPreferences.PASSWORD_FOR_MOBILE, lastPassword);
      cards.show("readonly");
    }
  }
}
