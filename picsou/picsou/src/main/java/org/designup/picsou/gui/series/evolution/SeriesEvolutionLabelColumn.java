package org.designup.picsou.gui.series.evolution;

import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.gui.series.view.SeriesWrapperType;
import org.designup.picsou.model.Month;
import org.globsframework.gui.views.GlobTableColumn;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.utils.directory.Directory;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.util.Comparator;

public class SeriesEvolutionLabelColumn implements GlobTableColumn {

  private GlobRepository repository;
  private Integer referenceMonthId;
  private SeriesEvolutionLabelEditor editor;

  public SeriesEvolutionLabelColumn(GlobTableView view,
                                    GlobRepository repository, Directory directory,
                                    SeriesEvolutionColors colors,
                                    SeriesEditionDialog seriesEditionDialog) {
    this.repository = repository;
    this.editor = new SeriesEvolutionLabelEditor(view, repository, directory, colors, seriesEditionDialog);
  }

  public void setReferenceMonthId(Integer monthId) {
    if ((monthId == null) || !repository.contains(Key.create(Month.TYPE, monthId))) {
      referenceMonthId = null;
    }
    else {
      referenceMonthId = Month.normalize(monthId);
    }
    editor.setReferenceMonth(referenceMonthId);
  }

  public String getName() {
    return "";
  }

  public TableCellRenderer getRenderer() {
    return editor;
  }

  public TableCellEditor getEditor() {
    return editor;
  }

  public GlobStringifier getStringifier() {
    return editor.getStringifier();
  }

  public Comparator<Glob> getComparator() {
    return null;
  }

  public boolean isEditable(int row, Glob seriesWrapper) {
    return SeriesWrapperType.SERIES.isOfType(seriesWrapper);
  }
}