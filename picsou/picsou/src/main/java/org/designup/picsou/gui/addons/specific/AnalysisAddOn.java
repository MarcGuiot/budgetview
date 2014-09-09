package org.designup.picsou.gui.addons.specific;

import org.designup.picsou.gui.addons.AddOn;
import org.designup.picsou.gui.card.CardView;
import org.designup.picsou.gui.components.tips.TipAnchor;
import org.designup.picsou.gui.components.tips.TipPosition;
import org.designup.picsou.gui.model.Card;
import org.designup.picsou.gui.signpost.SignpostService;
import org.designup.picsou.model.AddOns;
import org.designup.picsou.utils.Lang;
import org.globsframework.utils.directory.Directory;

public class AnalysisAddOn extends AddOn {
  public AnalysisAddOn() {
    super(AddOns.ANALYSIS, "addons/analysis.png");
  }

  protected void processPostActivation(Directory directory) {
    directory.get(SignpostService.class).show(CardView.getSignpostId(Card.ANALYSIS),
                                              Lang.get("addons.analysis.signpost"),
                                              TipPosition.BOTTOM_RIGHT,
                                              TipAnchor.SOUTH);
  }
}
