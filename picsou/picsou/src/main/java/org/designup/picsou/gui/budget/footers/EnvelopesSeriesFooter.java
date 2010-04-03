package org.designup.picsou.gui.budget.footers;

import org.globsframework.model.*;
import org.globsframework.model.utils.TypeChangeSetListener;
import org.designup.picsou.utils.Lang;
import org.designup.picsou.model.UserPreferences;

import javax.swing.*;
import java.util.List;

public class EnvelopesSeriesFooter implements BudgetAreaSeriesFooter {
  private JEditorPane editorPane;
  private GlobRepository repository;

  public EnvelopesSeriesFooter(GlobRepository repository) {
    this.repository = repository;
  }

  public void init(JEditorPane editorPane) {
    this.editorPane = editorPane;
    repository.addChangeListener(new TypeChangeSetListener(UserPreferences.TYPE) {
      protected void update(GlobRepository repository) {
        doUpdate();
      }
    });
  }

  public void update(List<Key> displayedKeys) {
  }

  private void doUpdate() {
    final Glob preferences = repository.find(UserPreferences.KEY);
    if (preferences == null) {
      return;
    }
    
    Boolean showMessage =
      preferences.get(UserPreferences.SHOW_ENVELOPES_EDITION_MESSAGE);
    editorPane.setVisible(showMessage);
    editorPane.setText(showMessage ? Lang.get("budgetview.envelopes.editAmounts") : "");    
  }
}
