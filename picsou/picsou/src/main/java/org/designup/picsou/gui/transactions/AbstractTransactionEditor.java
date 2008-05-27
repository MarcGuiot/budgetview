package org.designup.picsou.gui.transactions;

import org.crossbowlabs.globs.gui.views.GlobTableView;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.format.DescriptionService;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.designup.picsou.gui.utils.AbstractRolloverEditor;

public abstract class AbstractTransactionEditor extends AbstractRolloverEditor {
  protected TransactionRendererColors rendererColors;

  protected AbstractTransactionEditor(GlobTableView view, TransactionRendererColors transactionRendererColors,
                                      DescriptionService descriptionService, GlobRepository repository, Directory directory) {
    super(view, descriptionService, repository, directory);
    this.rendererColors = transactionRendererColors;
  }
}
