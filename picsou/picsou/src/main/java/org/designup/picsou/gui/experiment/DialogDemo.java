package org.designup.picsou.gui.experiment;

import org.designup.picsou.client.ServerAccess;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.PicsouInit;
import org.designup.picsou.gui.MainWindow;
import org.designup.picsou.gui.categories.CategoryEditionDialog;
import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Month;
import org.designup.picsou.utils.generator.PicsouSampleGenerator;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.GlobList;
import org.globsframework.utils.directory.Directory;
import org.globsframework.gui.splits.SplitsEditor;

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

    PicsouSampleGenerator generator = new PicsouSampleGenerator(repository);
    generator.run(200710, 200809);

    JFrame frame = window.getFrame();
    directory.add(JFrame.class, frame);

    showSeriesEditionDialog(repository, directory, frame);
//    showCategoriesEditionDialog(repository, directory);
  }

  private static void showSeriesEditionDialog(GlobRepository repository, Directory directory, JFrame frame) {
    SeriesEditionDialog dialog = new SeriesEditionDialog(frame, repository, directory);
    SplitsEditor.show(dialog.getDialog(), directory);
//    dialog.show(BudgetArea.EXPENSES_ENVELOPE, Collections.singleton(200808));
    dialog.show(BudgetArea.RECURRING_EXPENSES, Collections.singleton(200808));
  }

  private static void showCategoriesEditionDialog(GlobRepository repository, Directory directory) {
    CategoryEditionDialog dialog = new CategoryEditionDialog(repository, directory);
//    SplitsEditor.show(dialog.getDialog(), directory);
    dialog.show(GlobList.EMPTY);
  }
}
