package org.designup.picsou.gui.transactions;

import org.designup.picsou.gui.utils.AbstractRolloverEditor;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.utils.directory.Directory;

public abstract class AbstractTransactionEditor extends AbstractRolloverEditor {
  protected TransactionRendererColors rendererColors;

  protected AbstractTransactionEditor(GlobTableView view, TransactionRendererColors transactionRendererColors,
                                      DescriptionService descriptionService, GlobRepository repository, Directory directory) {
    super(view, descriptionService, repository, directory);
    this.rendererColors = transactionRendererColors;
  }
}
