package org.designup.picsou.gui.series.evolution;

import org.designup.picsou.gui.description.SeriesWrapperDescriptionStringifier;
import org.designup.picsou.gui.series.SeriesEditor;
import org.designup.picsou.gui.series.view.SeriesWrapperStringifier;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Collections;

public class SeriesEvolutionLabelEditor extends SeriesEvolutionEditor {

  private SeriesWrapperStringifier stringifier;
  private SeriesWrapperDescriptionStringifier descriptionStringifier;

  protected SeriesEvolutionLabelEditor(GlobTableView view,
                                       GlobRepository repository,
                                       Directory directory,
                                       SeriesEvolutionColors colors) {
    super(-1, view, directory.get(DescriptionService.class), repository, directory, colors);

    this.stringifier = new SeriesWrapperStringifier(repository, directory);
    this.descriptionStringifier = new SeriesWrapperDescriptionStringifier();

    complete(new OpenSeriesEditionDialogAction(directory));
  }

  protected String getText(Glob seriesWrapper) {
    return stringifier.toString(seriesWrapper, repository);
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