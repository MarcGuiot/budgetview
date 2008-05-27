package org.designup.picsou.gui.actions;

import org.crossbowlabs.globs.gui.SelectionService;
import org.crossbowlabs.globs.model.FieldValue;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.GlobRepositoryBuilder;
import org.crossbowlabs.globs.model.Key;
import static org.crossbowlabs.globs.model.FieldValue.*;
import static org.crossbowlabs.globs.model.FieldValue.value;
import org.crossbowlabs.globs.model.format.DescriptionService;
import org.crossbowlabs.globs.utils.directory.DefaultDirectory;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.splits.color.ColorService;
import org.designup.picsou.gui.utils.PicsouDescriptionService;
import org.designup.picsou.model.TransactionImport;

import javax.swing.*;
import java.util.Date;

public class QifBalancePanelDemo {

  public static void main(String[] args) {
    Directory directory = new DefaultDirectory();
    directory.add(DescriptionService.class, new PicsouDescriptionService());
    directory.add(new ColorService());
    SelectionService selectionService = new SelectionService();
    directory.add(selectionService);
    GlobRepository repository = GlobRepositoryBuilder.createEmpty();
    repository.create(Key.create(TransactionImport.TYPE, TransactionImport.ID),
                      value(TransactionImport.SOURCE, "/path/to/file"),
                      value(TransactionImport.LAST_TRANSACTION_DATE, new Date()));
    QifBalancePanel detailPanel = new QifBalancePanel(repository, directory,
                                                      Key.create(TransactionImport.TYPE, 1));
    JFrame jFrame = new JFrame();
    selectionService.select(repository.get(Key.create(TransactionImport.TYPE, 1)));
    detailPanel.showDialog(jFrame);
  }
}
