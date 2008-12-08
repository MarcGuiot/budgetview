package org.designup.picsou.gui.series.evolution;

import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.gui.series.view.SeriesWrapperType;
import org.designup.picsou.model.Month;
import org.globsframework.gui.views.GlobTableColumn;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.util.Comparator;

public class SeriesEvolutionMonthColumn implements GlobTableColumn {

  private int offset;
  private GlobRepository repository;
  private Integer referenceMonthId;
  private SeriesEvolutionMonthEditor editor;

  public SeriesEvolutionMonthColumn(int offset, GlobTableView view,
                                    GlobRepository repository, Directory directory,
                                    SeriesEvolutionColors colors, SeriesEditionDialog seriesEditionDialog) {
    this.offset = offset;
    this.repository = repository;
    this.editor = new SeriesEvolutionMonthEditor(offset, view, repository, directory, colors, seriesEditionDialog);
  }

  public void setReferenceMonthId(Integer monthId) {
    if ((monthId == null) || (repository.find(Key.create(Month.TYPE, monthId)) == null)) {
      referenceMonthId = null;
    }
    else {
      referenceMonthId = Month.normalize(monthId + offset);
    }
    editor.setReferenceMonth(referenceMonthId);
  }

  public String getName() {
    return Month.getShortMonthLabelWithYear(referenceMonthId);
  }

  public TableCellRenderer getRenderer() {
    return editor;
  }

  public TableCellEditor getEditor() {
    return editor;
  }

  public Comparator<Glob> getComparator() {
    return null;
  }

  public boolean isEditable(int row, Glob seriesWrapper) {
    return SeriesWrapperType.SERIES.isOfType(seriesWrapper);
  }
}