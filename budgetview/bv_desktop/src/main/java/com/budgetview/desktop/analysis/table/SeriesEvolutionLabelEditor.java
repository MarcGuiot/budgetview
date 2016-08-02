package com.budgetview.desktop.analysis.table;

import com.budgetview.desktop.analysis.SeriesChartsColors;
import com.budgetview.desktop.description.stringifiers.SeriesWrapperDescriptionStringifier;
import com.budgetview.desktop.series.SeriesEditor;
import com.budgetview.desktop.series.view.SeriesWrapper;
import com.budgetview.desktop.series.view.SeriesWrapperStringifier;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.event.ActionEvent;
import java.util.Collections;

public class SeriesEvolutionLabelEditor extends SeriesEvolutionEditor {

  private SeriesWrapperStringifier stringifier;
  private SeriesWrapperDescriptionStringifier descriptionStringifier;

  protected SeriesEvolutionLabelEditor(GlobTableView view,
                                       GlobRepository repository,
                                       Directory directory,
                                       SeriesChartsColors colors) {
    super(-1, view, directory.get(DescriptionService.class), repository, directory, colors);

    this.stringifier = new SeriesWrapperStringifier(repository, directory);
    this.descriptionStringifier = new SeriesWrapperDescriptionStringifier();

    complete(new OpenSeriesEditionDialogAction(directory));
  }

  protected String getText(Glob seriesWrapper) {
    return stringifier.toString(seriesWrapper, repository);
  }

  protected Border getBorderForSeries(Glob wrapper) {
    return SeriesWrapper.isGroupPart(wrapper, repository) ? BorderFactory.createEmptyBorder(0,5,0,0) : null;
  }

  protected String getDescription(Glob seriesWrapper) {
    return descriptionStringifier.toString(seriesWrapper, repository);
  }

  public GlobStringifier getStringifier() {
    return stringifier;
  }

  private class OpenSeriesEditionDialogAction extends AbstractAction {
    private Directory directory;

    public OpenSeriesEditionDialogAction(Directory directory) {
      this.directory = directory;
    }

    public void actionPerformed(ActionEvent e) {
      SeriesEditor.get(directory).showSeries(currentSeries, Collections.singleton(referenceMonthId));
    }
  }
}