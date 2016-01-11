package org.designup.picsou.gui.card;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.card.utils.NavigationAction;
import org.designup.picsou.gui.card.utils.NavigationIcons;
import org.designup.picsou.gui.help.HelpService;
import org.designup.picsou.gui.model.Card;
import org.designup.picsou.gui.signpost.PersistentSignpost;
import org.designup.picsou.gui.signpost.SignpostService;
import org.designup.picsou.gui.signpost.guides.GotoCategorizationSignpost;
import org.designup.picsou.gui.signpost.guides.SkipAndGotoBudgetSignpost;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.model.AddOns;
import org.designup.picsou.model.Month;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.ImageLocator;
import org.globsframework.gui.splits.PanelBuilder;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.gui.splits.repeat.Repeat;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.TypeChangeSetListener;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class CardView extends View implements GlobSelectionListener {
  private CardHandler cards;

  private Card lastSelectedCard = NavigationService.INITIAL_CARD;
  private JToggleButton[] toggles = new JToggleButton[Card.values().length];
private static final Card[] CARDS = {Card.HOME, Card.BUDGET, Card.DATA, Card.CATEGORIZATION, Card.PROJECTS, Card.ANALYSIS, Card.ADDONS};
  private PersistentSignpost categorizationCompletionSignpost;

  public CardView(GlobRepository repository, Directory directory, PersistentSignpost completionSignpost) {
    super(repository, directory);
    this.selectionService.addListener(this, Card.TYPE);
    this.categorizationCompletionSignpost = completionSignpost;
  }

  public void registerComponents(GlobsPanelBuilder builder) {

    cards = builder.addCardHandler("mainCards");

    final ButtonGroup masterGroup = new ButtonGroup();
    final ImageLocator images = directory.get(ImageLocator.class);
    final SignpostService signposts = directory.get(SignpostService.class);
    for (Card card : CARDS) {
      final JToggleButton toggle = new JToggleButton(new ToggleAction(card));
      toggle.setIcon(NavigationIcons.get(images, card));
      toggle.setRolloverEnabled(true);
      toggle.setToolTipText(getTooltip(card));
      String name = card.getName() + "CardToggle";
      Gui.configureIconButton(toggle, name, NavigationIcons.DIMENSION);
      masterGroup.add(toggle);
      toggles[card.getId()] = toggle;
      signposts.registerComponent(getSignpostId(card), toggle);

      switch (card) {
        case BUDGET:
          categorizationCompletionSignpost.attach(toggle);
          PersistentSignpost gotoBudgetSignpost = new SkipAndGotoBudgetSignpost(repository, directory);
          gotoBudgetSignpost.attach(toggle);
          break;
        case CATEGORIZATION:
          PersistentSignpost gotoCategorizationSignpost = new GotoCategorizationSignpost(repository, directory);
          gotoCategorizationSignpost.attach(toggle);
          break;
      }
    }

    final Repeat<Card> repeat = builder.addRepeat("viewToggles", getActiveCards(), new RepeatComponentFactory<Card>() {
      public void registerComponents(PanelBuilder cellBuilder, Card card) {
        cellBuilder.add("toggle", toggles[card.getId()]);
      }
    });
    repository.addChangeListener(new TypeChangeSetListener(AddOns.TYPE) {
      public void update(GlobRepository repository) {
        repeat.set(getActiveCards());
      }
    });

    addBackForwardActions(builder);

    showCard(NavigationService.INITIAL_CARD);
  }

  public static String getSignpostId(Card card) {
    return "card." + card.getName().toLowerCase();
  }

  public List<Card> getActiveCards() {
    List<Card> result = new ArrayList<Card>();
    for (Card card : CARDS) {
      if (((card == Card.PROJECTS) && !AddOns.isEnabled(AddOns.PROJECTS, repository)) ||
          ((card == Card.ANALYSIS) && !AddOns.isEnabled(AddOns.ANALYSIS, repository))) {
        continue;
      }
      result.add(card);
    }
    return result;
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
}

