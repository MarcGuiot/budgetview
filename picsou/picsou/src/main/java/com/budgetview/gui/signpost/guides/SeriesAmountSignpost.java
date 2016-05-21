package com.budgetview.gui.signpost.guides;

import com.budgetview.gui.signpost.SimpleSignpost;
import com.budgetview.model.SignpostStatus;
import com.budgetview.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class SeriesAmountSignpost extends SimpleSignpost {
  public SeriesAmountSignpost(GlobRepository repository, Directory directory) {
    super(Lang.get("signpost.seriesAmount"),
          SignpostStatus.SERIES_AMOUNT_SHOWN,
          SignpostStatus.GOTO_BUDGET_DONE,
          repository, directory);
  }
}