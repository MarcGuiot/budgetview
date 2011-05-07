package org.designup.picsou.gui.card;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.signpost.Signpost;
import org.designup.picsou.gui.card.utils.NavigationAction;
import org.designup.picsou.gui.card.utils.NavigationIcons;
import org.designup.picsou.gui.help.HelpService;
import org.designup.picsou.gui.model.Card;
import org.designup.picsou.gui.signpost.guides.GotoCategorizationSignpost;
import org.designup.picsou.gui.signpost.guides.GotoDataSignpost;
import org.designup.picsou.gui.signpost.guides.SkipAndGotoBudgetSignpost;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.model.Month;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.ImageLocator;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;

public class CardView extends View implements GlobSelectionListener {
  private CardHandler cards;

  private Card lastSelectedCard = NavigationService.INITIAL_CARD;
  private JToggleButton[] toggles = new JToggleButton[Card.values().length];
  private static final Card[] CARDS = {Card.HOME, Card.BUDGET, Card.SAVINGS, Card.DATA, Card.CATEGORIZATION, Card.ANALYSIS};
  private CardView.ViewHelpAction viewHelpAction;
  private Signpost categorizationCompletionSignpost;

  public CardView(GlobRepository repository, Directory directory, Signpost completionSignpost) {
    super(repository, directory);
    this.selectionService.addListener(this, Card.TYPE);
    this.viewHelpAction = new ViewHelpAction();
    this.categorizationCompletionSignpost = completionSignpost;
  }

  public void registerComponents(GlobsPanelBuilder builder) {

    cards = builder.addCardHandler("mainCards");

    final ButtonGroup masterGroup = new ButtonGroup();
    final ImageLocator images = directory.get(ImageLocator.class);
    for (Card card : CARDS) {
      JToggleButton toggle = new JToggleButton(new ToggleAction(card));
      toggle.setIcon(NavigationIcons.get(images, card));
      toggle.setRolloverEnabled(true);
      toggle.setRolloverIcon(NavigationIcons.getRollover(images, card));
      toggle.setToolTipText(getTooltip(card));
      String name = card.getName() + "CardToggle";
      Gui.configureIconButton(toggle, name, NavigationIcons.DIMENSION);
      masterGroup.add(toggle);
      toggles[card.getId()] = toggle;

      if (card.equals(Card.BUDGET)) {
        categorizationCompletionSignpost.attach(toggle);
        Signpost signpost = new SkipAndGotoBudgetSignpost(repository, directory);
        signpost.attach(toggle);
      }
      else if (card.equals(Card.DATA)) {
        Signpost signpost = new GotoDataSignpost(repository, directory);
        signpost.attach(toggle);
      }
      else if (card.equals(Card.CATEGORIZATION)) {
        Signpost signpost = new GotoCategorizationSignpost(repository, directory);
        signpost.attach(toggle);
      }
    }

    builder.addRepeat("viewToggles", Arrays.asList(CARDS), new RepeatComponentFactory<Card>() {
      public void registerComponents(RepeatCellBuilder cellBuilder, Card card) {
        cellBuilder.add("toggle", toggles[card.getId()]);
      }
    });

    addBackForwardActions(builder);

    showCard(NavigationService.INITIAL_CARD);
  }

  public void showInitialCard() {
    showCard(NavigationService.INITIAL_CARD);
  }

  private String getTooltip(Card card) {
    return "<html>" +
           "<div style=\"margin:5px\">" +
           "<p style=\"text-align:center\"><b>" + card.getLabel() + "</b></p>" +
           "<hr>" +
           "<p>" + card.getDescription() + "</p>" +
           "</div>" +
           "</html>";
  }

  private void addBackForwardActions(GlobsPanelBuilder builder) {
    builder.add("backView", new NavigationAction(directory) {
      protected boolean getEnabledState(NavigationService navigationService) {
        return navigationService.backEnabled();
      }

      protected void apply(NavigationService navigationService) {
        navigationService.back();
      }
    });

    builder.add("forwardView", new NavigationAction(directory) {
      protected boolean getEnabledState(NavigationService navigationService) {
        return navigationService.forwardEnabled();
      }

      protected void apply(NavigationService navigationService) {
        navigationService.forward();
      }
    });
  }

  private void showCard(Card card) {
    toggles[card.getId()].doClick(0);
    viewHelpAction.setCurrentCard(card);
  }

  public void selectionUpdated(GlobSelection selection) {
    if (selection.isRelevantForType(Card.TYPE)) {
      GlobList cards = selection.getAll(Card.TYPE);
      if (cards.size() == 1) {
        showCard(Card.get(cards.get(0).get(Card.ID)));
      }
    }
    if (selection.isRelevantForType(Month.TYPE)) {
      showCard(lastSelectedCard);
    }
  }

  public Action getHelpAction() {
    return viewHelpAction;
  }

  private class ToggleAction extends AbstractAction {
    private Card card;

    public ToggleAction(Card card) {
      super(card.getLabel());
      this.card = card;
    }

    public void actionPerformed(ActionEvent e) {
      if (card == lastSelectedCard) {
        return;
      }

      lastSelectedCard = card;
      cards.show(card.getName());
      selectionService.select(repository.get(card.getKey()));
    }
  }

  private class ViewHelpAction extends AbstractAction {

    private ViewHelpAction() {
      super(Lang.get("help"));
    }

    public void setCurrentCard(Card card) {
      putValue(Action.NAME, Lang.get("cards.help", card.getLabel()));
    }

    public void actionPerformed(ActionEvent e) {
      String helpRef = "card_" + lastSelectedCard.getName();
      directory.get(HelpService.class).show(helpRef, directory.get(JFrame.class));
    }
  }
}

