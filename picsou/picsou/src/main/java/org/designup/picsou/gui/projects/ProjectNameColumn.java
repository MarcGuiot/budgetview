package org.designup.picsou.gui.projects;

import org.designup.picsou.gui.components.HyperlinkTableColumn;
import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.model.SeriesBudget;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.splits.components.HyperlinkButton;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.Comparator;
import java.util.Collections;

public class ProjectNameColumn extends HyperlinkTableColumn {
  private GlobStringifier stringifier;
  private Glob seriesBudget;
  private SeriesEditionDialog seriesEditionDialog;

  public ProjectNameColumn(GlobTableView view, DescriptionService descriptionService, GlobRepository repository, Directory directory) {
    super(view, descriptionService, repository, directory);
    stringifier = descriptionService.getStringifier(SeriesBudget.SERIES);
    seriesEditionDialog = new SeriesEditionDialog(repository, directory);
  }

  public String getName() {
    return Lang.get("nextprojects.series");
  }

  public GlobStringifier getStringifier() {
    return stringifier;
  }

  public Comparator<Glob> getComparator() {
    return stringifier.getComparator(repository);
  }

  protected void updateComponent(HyperlinkButton button, JPanel panel, Glob seriesBudget, boolean render) {
    if (!render) {
      this.seriesBudget = seriesBudget;
    }

    button.setText(stringifier.toString(seriesBudget, repository));
  }

  protected void processClick() {
    final Glob series = repository.findLinkTarget(seriesBudget, SeriesBudget.SERIES);
    seriesEditionDialog.show(series, Collections.singleton(seriesBudget.get(SeriesBudget.MONTH)));
  }
}
