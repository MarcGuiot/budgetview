package org.designup.picsou.gui.signpost.guides;

import net.java.balloontip.BalloonTip;
import org.designup.picsou.gui.signpost.SimpleSignpost;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SignpostStatus;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class CategorizationAreaSignpost extends SimpleSignpost {
  public CategorizationAreaSignpost(GlobRepository repository, Directory directory) {
    super(Lang.get("signpost.categorizationAreaSelection"),
          SignpostStatus.CATEGORIZATION_AREA_SELECTION_DONE,
          SignpostStatus.CATEGORIZATION_SELECTION_DONE,
          repository, directory);
    setLocation(BalloonTip.Orientation.RIGHT_ABOVE, BalloonTip.AttachLocation.NORTH);
  }

  protected void update() {
    if (!SignpostStatus.isCompleted(SignpostStatus.CATEGORIZATION_AREA_SELECTION_DONE, repository) &&
        SignpostStatus.isCompleted(prerequisiteField, repository)) {
      GlobList transactions = selectionService.getSelection(Transaction.TYPE);
      if (transactions.size() > 0) {
        Glob transaction = transactions.get(0);
        Integer seriesId = transaction.get(Transaction.SERIES);
        if (!Series.UNCATEGORIZED_SERIES_ID.equals(seriesId)) {
          repository.startChangeSet();
          try {
            SignpostStatus.setCompleted(SignpostStatus.CATEGORIZATION_AREA_SELECTION_DONE, repository);
            SignpostStatus.setCompleted(SignpostStatus.FIRST_CATEGORIZATION_DONE, repository);
          }
          finally {
            repository.completeChangeSet();
          }
          return;
        }
      }
    }
    super.update();
  }
}