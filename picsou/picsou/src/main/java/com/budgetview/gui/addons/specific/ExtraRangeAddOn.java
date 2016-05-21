package com.budgetview.gui.addons.specific;

import com.budgetview.gui.model.Card;
import com.budgetview.gui.addons.AddOn;
import com.budgetview.gui.card.CardView;
import com.budgetview.gui.components.tips.TipAnchor;
import com.budgetview.gui.components.tips.TipPosition;
import com.budgetview.gui.signpost.SignpostService;
import com.budgetview.model.AddOns;
import com.budgetview.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class ExtraRangeAddOn extends AddOn {
  public ExtraRangeAddOn() {
    super(AddOns.EXTRA_RANGE, "addons/extrarange.png");
  }

  protected void processPostActivation(GlobRepository repository, Directory directory) {
    directory.get(SignpostService.class).show(CardView.getSignpostId(Card.BUDGET),
                                              Lang.get("addons.extrarange.signpost"),
                                              TipPosition.BOTTOM_RIGHT,
                                              TipAnchor.SOUTH);
  }
}
