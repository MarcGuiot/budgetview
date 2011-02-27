package org.designup.picsou.gui.categorization.special;

import org.designup.picsou.gui.categorization.utils.SeriesCreationHandler;
import org.designup.picsou.gui.help.HyperlinkHandler;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.ProfileType;
import org.designup.picsou.model.Series;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.FieldValuesBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class InternalTransfersCategorizationPanel extends HtmlCategorizationPanel {
  public InternalTransfersCategorizationPanel() {
    super("internalTransfers");
  }

  protected void registerHyperlinkActions(HyperlinkHandler handler,
                                          GlobRepository repository,
                                          Directory directory,
                                          final SeriesCreationHandler seriesCreationHandler) {
    handler.registerLinkAction("createInternalTransfersSeries", new Runnable() {
      public void run() {
        seriesCreationHandler.createSeries(BudgetArea.VARIABLE,
                                           FieldValuesBuilder.init()
                                             .set(Series.NAME, Lang.get("categorization.specialCases.internalTransfers.seriesName"))
                                             .set(Series.PROFILE_TYPE, ProfileType.IRREGULAR.getId())
                                             .set(Series.IS_AUTOMATIC, false)
                                             .toArray());
      }
    });
  }
}
