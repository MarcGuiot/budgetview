package org.designup.picsou.gui.categorization.components;

import org.designup.picsou.gui.utils.SetFieldValueAction;
import org.designup.picsou.model.UserPreferences;
import org.designup.picsou.utils.Lang;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;

import javax.swing.*;
import java.util.Set;

public class DescriptionPanelHandler {

  private JPanel panel = new JPanel();
  private Action showAction;
  private Action hideAction;
  private GlobRepository repository;

  public DescriptionPanelHandler(GlobRepository repository) {
    this.repository = repository;

    showAction = new SetFieldValueAction(Lang.get("categorization.showDescriptionButton"),
                                         UserPreferences.KEY,
                                         UserPreferences.SHOW_BUDGET_AREA_DESCRIPTIONS,
                                         true,
                                         repository);
    hideAction = new SetFieldValueAction(UserPreferences.KEY,
                                         UserPreferences.SHOW_BUDGET_AREA_DESCRIPTIONS,
                                         false,
                                         repository);

    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(UserPreferences.TYPE)) {
          update();
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        if (changedTypes.contains(UserPreferences.TYPE)) {
          update();
        }
      }
    });
  }

  public JPanel getPanel() {
    return panel;
  }

  public Action getShowAction() {
    return showAction;
  }

  public Action getHideAction() {
    return hideAction;
  }

  private void update() {
    Glob userPreferences = repository.find(UserPreferences.KEY);

    boolean showDescription =
      (userPreferences == null) || userPreferences.isTrue(UserPreferences.SHOW_BUDGET_AREA_DESCRIPTIONS);

    panel.setVisible(showDescription);
    showAction.setEnabled(!showDescription);
  }
}
