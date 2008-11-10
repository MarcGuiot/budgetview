package org.designup.picsou.gui.experiment;

import org.designup.picsou.client.ServerAccess;
import org.designup.picsou.gui.MainWindow;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.PicsouInit;
import org.designup.picsou.gui.license.LicenseExpirationDialog;
import org.designup.picsou.gui.categories.CategoryEditionDialog;
import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.gui.help.HelpService;
import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.gui.series.wizard.SeriesWizardDialog;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Month;
import org.designup.picsou.utils.generator.PicsouSampleGenerator;
import org.globsframework.gui.splits.SplitsEditor;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.Collections;

public class DialogDemo {
  public static void main(String[] args) throws Exception {

    MainWindow window = new MainWindow();
    PicsouDialog.FORCE_NONMODAL = true;
    PicsouApplication.parseLanguage("-l", "fr");
    Directory directory = PicsouApplication.createDirectory();
    GlobRepository repository = PicsouInit.init(ServerAccess.NULL, "user", true, directory).getRepository();
    repository.enterBulkDispatchingMode();
    for (int monthId = 200701; monthId < 200812; monthId = Month.next(monthId)) {
      repository.findOrCreate(Key.create(Month.TYPE, monthId));
    }
    repository.completeBulkDispatchingMode();
    directory.add(new HelpService(repository, directory));

    PicsouSampleGenerator generator = new PicsouSampleGenerator(repository);
    generator.run(200710, 200809);

    JFrame frame = window.getFrame();
    directory.add(JFrame.class, frame);

//    showHelpDialog(repository, directory, frame);
//    showSeriesWizardDialog(repository, directory, frame);
//    showSeriesEditionDialog(repository, directory, frame);
    showLicenseExpirationDialog(repository, directory, frame);
//    showCategoriesEditionDialog(repository, directory);
  }

  private static void showHelpDialog(GlobRepository repository, Directory directory, JFrame frame) {
    HelpService service = directory.get(HelpService.class);
    service.show("intro", frame);
  }

  private static void showSeriesWizardDialog(GlobRepository repository, Directory directory, JFrame frame) {
    SeriesWizardDialog dialog = new SeriesWizardDialog(repository, directory);
//    SplitsEditor.show(dialog.getDialog(), directory);
    dialog.show();
  }

  private static void showLicenseExpirationDialog(GlobRepository repository, Directory directory, JFrame frame) {
    LicenseExpirationDialog dialog = new LicenseExpirationDialog(frame, repository, directory);
    dialog.show();
  }

  private static void showSeriesEditionDialog(GlobRepository repository, Directory directory, JFrame frame) {
    SeriesEditionDialog dialog = new SeriesEditionDialog(frame, repository, directory);
    SplitsEditor.show(dialog.getDialog(), directory);
//    dialog.show(BudgetArea.EXPENSES_ENVELOPE, Collections.singleton(200808));
    SplitsEditor.show(dialog.getDialog(), directory);
    dialog.show(BudgetArea.RECURRING, Collections.singleton(200808), null);
  }

  private static void showCategoriesEditionDialog(GlobRepository repository, Directory directory) {
    CategoryEditionDialog dialog = new CategoryEditionDialog(repository, directory);
//    SplitsEditor.show(dialog.getDialog(), directory);
    dialog.show(GlobList.EMPTY);
  }
}
