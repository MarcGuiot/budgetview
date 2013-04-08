package org.designup.picsou.gui.preferences;

import org.designup.picsou.gui.components.dialogs.MessageDialog;
import org.designup.picsou.gui.components.dialogs.MessageType;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.gui.preferences.components.ColorThemeItemFactory;
import org.designup.picsou.gui.startup.AppPaths;
import org.designup.picsou.model.ColorTheme;
import org.designup.picsou.model.NumericDateType;
import org.designup.picsou.model.TextDateType;
import org.designup.picsou.model.UserPreferences;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.model.repository.LocalGlobRepositoryBuilder;
import org.globsframework.model.utils.GlobComparators;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Ref;
import org.globsframework.utils.Strings;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class PreferencesDialog {
  private final JLabel pathToDataLabel;
  private final AbstractAction backToDefaultPathAction;
  private PicsouDialog dialog;

  private LocalGlobRepository repository;

  private GlobsPanelBuilder builder;
  private ColorThemeItemFactory colorThemeFactory;
  private GlobRepository parentRepository;
  private Directory directory;

  public PreferencesDialog(Window parent, final GlobRepository parentRepository, final Directory directory) {
    this.parentRepository = parentRepository;
    this.directory = directory;
    this.repository = createLocalRepository(parentRepository);

    builder = new GlobsPanelBuilder(PreferencesDialog.class,
                                    "/layout/general/preferencesDialog.splits",
                                    repository, directory);
    int[] items = new int[]{12, 18, 24, 36};
    Utils.beginRemove();
    items = new int[]{0, 1, 2, 3, 4, 6, 12, 18, 24, 36};
    Utils.endRemove();

    builder.addComboEditor("futureMonth", UserPreferences.KEY,
                           UserPreferences.FUTURE_MONTH_COUNT, items);

    colorThemeFactory = new ColorThemeItemFactory(repository, directory);

    builder.addRepeat("colorThemes", ColorTheme.TYPE, GlobMatchers.ALL, GlobComparators.ascending(ColorTheme.ID),
                      colorThemeFactory);

    builder.addComboEditor("textDate", UserPreferences.TEXT_DATE_TYPE)
      .forceSelection(UserPreferences.KEY);

    builder.addComboEditor("numericDate", UserPreferences.NUMERIC_DATE_TYPE)
      .forceSelection(UserPreferences.KEY);

    builder.add("lang", createLangCombo(directory));

    pathToDataLabel = new JLabel();
    builder.add("pathToData", pathToDataLabel);
    builder.add("browsePathToData", new BrowseDataPath());
    backToDefaultPathAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        pathToDataLabel.setText("");
        backToDefaultPathAction.setEnabled(false);
      }
    };
    builder.add("backToDefault", backToDefaultPathAction);
    String redirect = AppPaths.getRedirect();
    if (Strings.isNotEmpty(redirect)){
      pathToDataLabel.setText(redirect);
      backToDefaultPathAction.setEnabled(true);
    }
    else {
      backToDefaultPathAction.setEnabled(false);
    }

    dialog = PicsouDialog.create(parent, directory);
    dialog.addPanelWithButtons((JPanel)builder.load(), new OkAction(), new CancelAction());
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

  private static LocalGlobRepository createLocalRepository(GlobRepository parentRepository) {
    return LocalGlobRepositoryBuilder.init(parentRepository)
      .copy(UserPreferences.TYPE, ColorTheme.TYPE, NumericDateType.TYPE, TextDateType.TYPE)
      .get();
  }

  public void show() {
    colorThemeFactory.init();
    dialog.pack();
    dialog.showCentered();
    builder.dispose();
  }

  private class OkAction extends AbstractAction {
    public OkAction() {
      super(Lang.get("ok"));
    }

    public void actionPerformed(ActionEvent e) {
      repository.commitChanges(true);
      colorThemeFactory.complete(parentRepository);
      boolean exit = false;
      if (!pathToDataLabel.getText().trim().equals(Strings.toString(AppPaths.getRedirect()).trim())) {
        String pathToData = pathToDataLabel.getText();
        Ref<String> message = new Ref<String>();
        if (Strings.isNullOrEmpty(pathToData) || AppPaths.newRedirect(pathToData, message)) {
          if (AppPaths.updateRedirect(pathToData, message)){
            MessageDialog.show("data.path.title", MessageType.INFO, dialog, directory, "data.path.exit");
            exit = true;
          }else {
            MessageDialog.showMessage("data.path.title", MessageType.ERROR, dialog, directory, message.get());
            return;
          }
        }
        else {
          MessageDialog.showMessage("data.path.title", MessageType.ERROR, dialog, directory, message.get());
          return;
        }
      }
      dialog.setVisible(false);
      if (exit) {
        JFrame frame = directory.get(JFrame.class);
        frame.setVisible(false);
        frame.dispose();
      }
    }
  }

  private class CancelAction extends AbstractAction {
    public CancelAction() {
      super(Lang.get("cancel"));
    }

    public void actionPerformed(ActionEvent e) {
      colorThemeFactory.complete(parentRepository);
      dialog.setVisible(false);
    }
  }

  private class BrowseDataPath extends AbstractAction {
    private BrowseDataPath() {
      super(Lang.get("browse"));
    }

    public void actionPerformed(ActionEvent e) {
      JFileChooser chooser = new JFileChooser();
      String path = AppPaths.getRedirect();
      if (path != null && new File(path).exists()) {
        chooser.setCurrentDirectory(new File(path));
      }
      chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      chooser.setMultiSelectionEnabled(false);
      int returnVal = chooser.showOpenDialog(dialog);
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        File file = chooser.getSelectedFile();
        if (file != null) {
          pathToDataLabel.setText(file.getAbsolutePath());
          backToDefaultPathAction.setEnabled(true);
        }
      }
    }
  }
}
