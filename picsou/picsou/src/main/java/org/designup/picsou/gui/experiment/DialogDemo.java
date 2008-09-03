package org.designup.picsou.gui.experiment;

import org.designup.picsou.client.ServerAccess;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.PicsouInit;
import org.designup.picsou.gui.categorization.CategorizationDialog;
import org.designup.picsou.gui.categories.CategoryEditionDialog;
import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.Category;
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

    PicsouDialog.MODAL = false;
    Directory directory = PicsouApplication.createDirectory();
    GlobRepository repository = PicsouInit.init(ServerAccess.NULL, "user", true, directory).getRepository();
    for (int monthId = 200701; monthId < 200809; monthId = Month.next(monthId)) {
      repository.findOrCreate(Key.create(Month.TYPE, monthId));
    }

    PicsouSampleGenerator generator = new PicsouSampleGenerator(repository);
    generator.run(200710, 200809);

    JFrame frame = new JFrame();
    directory.add(frame);

    CategorizationDialog dialog = new CategorizationDialog(frame, repository, directory);
    SplitsEditor.show(dialog.getDialog(), directory);
    dialog.show(repository.getAll(Transaction.TYPE), false, false);
  }
}
