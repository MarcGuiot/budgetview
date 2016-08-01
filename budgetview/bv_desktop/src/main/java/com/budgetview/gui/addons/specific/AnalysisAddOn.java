package com.budgetview.gui.addons.specific;

import com.budgetview.gui.addons.AddOn;
import com.budgetview.gui.components.tips.TipAnchor;
import com.budgetview.gui.components.tips.TipPosition;
import com.budgetview.gui.model.Card;
import com.budgetview.gui.card.CardView;
import com.budgetview.gui.signpost.SignpostService;
import com.budgetview.model.AddOns;
import com.budgetview.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class AnalysisAddOn extends AddOn {
  public AnalysisAddOn() {
    super(AddOns.ANALYSIS, "addons/analysis.png");
  }

  protected void processPostActivation(GlobRepository repository, Directory directory) {
    directory.get(SignpostService.class).show(CardView.getSignpostId(Card.ANALYSIS),
                                              Lang.get("addons.analysis.signpost"),
                                              TipPosition.BOTTOM_RIGHT,
                                              TipAnchor.SOUTH);
  }
}
