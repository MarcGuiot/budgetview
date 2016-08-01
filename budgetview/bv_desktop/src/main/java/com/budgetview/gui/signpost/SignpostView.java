package com.budgetview.gui.signpost;

import com.budgetview.gui.View;
import com.budgetview.gui.signpost.sections.BudgetSectionPanel;
import com.budgetview.gui.signpost.sections.CategorizationSectionPanel;
import com.budgetview.gui.signpost.sections.ImportSectionPanel;
import com.budgetview.gui.signpost.sections.SignpostSectionPanel;
import com.budgetview.model.SignpostStatus;
import com.budgetview.gui.help.HyperlinkHandler;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.PanelBuilder;
import org.globsframework.gui.splits.SplitsLoader;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SignpostView extends View {

  private CardHandler cardHandler;
  private List<SignpostSectionPanel> sections = new ArrayList<SignpostSectionPanel>();

  public SignpostView(GlobRepository repository, Directory directory) {
    super(repository, directory);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {

    cardHandler = parentBuilder.addCardHandler("signpostCard");

    initSections();

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/signpost/signpostView.splits",
                                                      repository, directory);

    builder.add("hyperlinkHandler", new HyperlinkHandler(directory));

    builder.addRepeat("signpostRepeat",
                      sections,
                      new RepeatComponentFactory<SignpostSectionPanel>() {
                        public void registerComponents(PanelBuilder cellBuilder,
                                                       SignpostSectionPanel section) {
                          section.registerComponents(cellBuilder);
                        }
                      });

    parentBuilder.add("signpostView", builder);

    builder.addLoader(new SplitsLoader() {
      public void load(Component component, SplitsNode node) {
        for (SignpostSectionPanel section : sections) {
          section.init();
        }
        repository.addChangeListener(new ChangeSetListener() {
          public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
            if (changeSet.containsChanges(SignpostStatus.TYPE)) {
              updateCard();
            }
          }

          public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
            if (changedTypes.contains(SignpostStatus.TYPE)) {
              updateCard();
            }
          }
        });
        updateCard();
      }
    });
  }

  public void reset() {
    updateCard();
  }

  private void updateCard() {
    if (SignpostStatus.isOnboardingCompleted(repository)) {
      cardHandler.show("dashboard");
    }
    else if (SignpostStatus.isCompleted(SignpostStatus.WELCOME_SHOWN, repository)) {
      cardHandler.show("signposts");
    }
    else {
      cardHandler.show("intro");
    }
  }

  private void initSections() {
    sections.add(new ImportSectionPanel(repository, directory));
    sections.add(new CategorizationSectionPanel(repository, directory));
    sections.add(new BudgetSectionPanel(repository, directory));
  }
}
