package com.budgetview.desktop.preferences.panes;

import com.budgetview.desktop.config.ConfigService;
import com.budgetview.desktop.preferences.PreferencesDialog;
import com.budgetview.desktop.preferences.PreferencesPane;
import com.budgetview.desktop.preferences.PreferencesResult;
import com.budgetview.model.UserPreferences;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ParametersPane implements PreferencesPane {
  private JPanel panel;

  public ParametersPane(GlobRepository repository, Directory directory) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(PreferencesDialog.class,
                                                      "/layout/general/preferences/parametersPane.splits",
                                                      repository, directory);

    int[] items = new int[]{12, 18, 24, 36, 48, 60};
    Utils.beginRemove();
    items = new int[]{0, 1, 2, 3, 4, 6, 12, 18, 24, 36, 48, 60};
    Utils.endRemove();

    builder.addComboEditor("futureMonth", UserPreferences.KEY,
                           UserPreferences.FUTURE_MONTH_COUNT, items);

    builder.addComboEditor("textDate", UserPreferences.TEXT_DATE_TYPE)
      .forceSelection(UserPreferences.KEY);

    builder.addComboEditor("numericDate", UserPreferences.NUMERIC_DATE_TYPE)
      .forceSelection(UserPreferences.KEY);

    builder.add("lang", createLangCombo(directory));

    panel = builder.load();
  }

  public JPanel getPanel() {
    return panel;
  }

  public void prepareForDisplay() {
  }

  public void validate(PreferencesResult result) {
  }

  public void postValidate() {
  }

  public void processCancel() {
  }

  private JComboBox createLangCombo(final Directory directory) {
    final String langEn = Lang.get("lang.en");
    final String langFr = Lang.get("lang.fr");
    final JComboBox langCombo = new JComboBox(new String[]{langEn, langFr});
    if ("fr".equalsIgnoreCase(Lang.getLocale().getCountry())) {
      langCombo.setSelectedItem(langFr);
    }
    else {
      langCombo.setSelectedItem(langEn);
    }
    langCombo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String lang = langCombo.getSelectedItem().toString();
        if (lang.equalsIgnoreCase(langEn)) {
          directory.get(ConfigService.class).setLang("en");
        }
        else {
          directory.get(ConfigService.class).setLang("fr");
        }
      }
    });
    return langCombo;
  }

}
