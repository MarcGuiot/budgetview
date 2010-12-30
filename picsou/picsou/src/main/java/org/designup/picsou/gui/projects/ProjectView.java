package org.designup.picsou.gui.projects;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.description.MonthListStringifier;
import org.designup.picsou.gui.description.MonthRangeFormatter;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Project;
import org.designup.picsou.model.ProjectItem;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.views.GlobButtonView;
import org.globsframework.gui.views.GlobLabelView;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.utils.GlobListFunctor;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.SortedSet;

public class ProjectView extends View {

  private ProjectEditionDialog editionDialog;

  public ProjectView(GlobRepository repository, Directory directory) {
    super(repository, directory);
    this.editionDialog = new ProjectEditionDialog(repository, directory);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/projects/projectView.splits",
                                                      repository, directory);

    builder.addRepeat("projectRepeat", Project.TYPE, GlobMatchers.ALL, new ProjectComponentFactory());

    builder.add("createProject", new CreateProjectAction());

    parentBuilder.add("projectView", builder);
  }

  private class ProjectComponentFactory implements RepeatComponentFactory<Glob> {
    public void registerComponents(RepeatCellBuilder cellBuilder, Glob project) {
      final Key projectKey = project.getKey();

      cellBuilder.add("projectName",
                      GlobButtonView.init(Project.TYPE, repository, directory, new GlobListFunctor() {
                        public void run(GlobList list, GlobRepository repository) {
                          editionDialog.show(projectKey);
                        }
                      })
                        .forceSelection(projectKey)
                        .getComponent());

      cellBuilder.add("projectPeriod",
                      GlobLabelView.init(Project.TYPE, repository, directory,
                                         new ProjectPeriodStringifier())
                        .forceSelection(projectKey)
                        .getComponent());

      cellBuilder.add("projectAmount",
                      GlobLabelView.init(Project.TYPE, repository, directory,
                                         new ProjectAmountStringifier())
                        .forceSelection(projectKey)
                        .getComponent());
    }

  }

  private class ProjectAmountStringifier implements GlobListStringifier {

    public String toString(GlobList list, GlobRepository repository) {
      double total = 0.0;
      for (Glob project : list) {
        total += project.get(Project.TOTAL_AMOUNT);
      }
      return Formatting.toString(total, BudgetArea.EXTRAS);
    }
  }

  private class ProjectPeriodStringifier implements GlobListStringifier {
    public String toString(GlobList list, GlobRepository repository) {
      if (list.isEmpty()) {
        return "";
      }
      Glob project = list.getFirst();
      GlobList items = repository.findLinkedTo(project, ProjectItem.PROJECT);
      SortedSet<Integer> months = items.getSortedSet(ProjectItem.MONTH);
      if (months.isEmpty()) {
        return "";
      }

      return MonthListStringifier.toString(months.first(), months.last(), MonthRangeFormatter.COMPACT);
    }
  }

  private class CreateProjectAction extends AbstractAction {
    private CreateProjectAction() {
      super(Lang.get("projectView.create"));
    }

    public void actionPerformed(ActionEvent actionEvent) {
      editionDialog.showNewProject();
    }
  }
}
