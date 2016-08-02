package com.budgetview.desktop.addons.specific;

import com.budgetview.desktop.addons.AddOn;
import com.budgetview.desktop.card.CardView;
import com.budgetview.desktop.components.tips.TipAnchor;
import com.budgetview.desktop.components.tips.TipPosition;
import com.budgetview.desktop.model.Card;
import com.budgetview.desktop.signpost.SignpostService;
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
