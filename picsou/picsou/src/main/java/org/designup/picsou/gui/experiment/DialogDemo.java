package org.designup.picsou.gui.experiment;

import org.designup.picsou.client.ServerAccess;
import org.designup.picsou.gui.MainWindow;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.PicsouInit;
import org.designup.picsou.gui.actions.ImportFileAction;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.help.HelpService;
import org.designup.picsou.gui.license.LicenseExpirationDialog;
import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Bank;
import org.designup.picsou.utils.generator.PicsouSampleGenerator;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class DialogDemo {
  public static void main(String[] args) throws Exception {

    Directory directory = PicsouApplication.createDirectory();
    MainWindow window = new MainWindow(null, "", "", false, directory);
    PicsouDialog.FORCE_NONMODAL = true;
    PicsouApplication.parseLanguage("-l", "fr");
    GlobRepository repository = PicsouInit.init(ServerAccess.NULL, directory, true).getRepository();
    repository.startChangeSet();
    for (int monthId = 200701; monthId < 200812; monthId = Month.next(monthId)) {
      repository.findOrCreate(Key.create(Month.TYPE, monthId));
    }
    repository.completeChangeSet();
    directory.add(new HelpService(repository, directory));

    PicsouSampleGenerator generator = new PicsouSampleGenerator(repository);
    generator.run(200710, 200809);

    JFrame frame = window.getFrame();
    directory.add(JFrame.class, frame);

//    showImportDialog(repository, directory, frame);
    showHelpDialog(repository, directory, frame);
//    showSeriesWizardDialog(repository, directory, frame);
//    showSeriesEditionDialog(repository, directory, frame);
//    showLicenseExpirationDialog(repository, directory, frame);
//    showCategoriesEditionDialog(repository, directory);
  }

  private static void showImportDialog(GlobRepository repository, Directory directory, JFrame frame) {
    ImportFileAction action = ImportFileAction.initForMenu("Import", repository, directory);
    action.actionPerformed(null);
  }

  private static void showHelpDialog(GlobRepository repository, Directory directory, JFrame frame) {
    HelpService service = directory.get(HelpService.class);
    service.showBankHelp(repository.get(Key.create(Bank.TYPE, 5)), frame);
  }

  private static void showLicenseExpirationDialog(GlobRepository repository, Directory directory, JFrame frame) {
    LicenseExpirationDialog dialog = new LicenseExpirationDialog(frame, repository, directory);
    dialog.show();
  }

  private static void showSeriesEditionDialog(GlobRepository repository, Directory directory, JFrame frame) {
    SeriesEditionDialog dialog = new SeriesEditionDialog(frame, repository, directory);
//    SplitsEditor.show(dialog.getDialog(), directory);
    dialog.showNewSeries(GlobList.EMPTY, new GlobList(200808), BudgetArea.SAVINGS);
  }
}
