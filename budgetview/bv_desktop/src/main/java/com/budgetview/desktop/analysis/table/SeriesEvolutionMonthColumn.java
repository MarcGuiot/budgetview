package com.budgetview.desktop.analysis.table;

import com.budgetview.desktop.analysis.SeriesChartsColors;
import com.budgetview.desktop.series.view.SeriesWrapperType;
import com.budgetview.model.Month;
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

public class SeriesEvolutionMonthColumn implements GlobTableColumn {

  private int offset;
  private GlobRepository repository;
  private Integer referenceMonthId;
  private SeriesEvolutionMonthEditor editor;

  public SeriesEvolutionMonthColumn(int offset, GlobTableView view,
                                    GlobRepository repository, Directory directory,
                                    SeriesChartsColors colors) {
    this.offset = offset;
    this.repository = repository;
    this.editor = new SeriesEvolutionMonthEditor(offset, view, repository, directory, colors);
  }

  public void setReferenceMonthId(Integer monthId) {
    if ((monthId == null) || !repository.contains(Key.create(Month.TYPE, monthId))) {
      referenceMonthId = null;
    }
    else {
      referenceMonthId = Month.offset(monthId, offset);
    }
    editor.setReferenceMonth(referenceMonthId);
  }

  public String getName() {
    return Month.getShortMonthLabelWithYear(referenceMonthId);
  }

  public Integer getReferenceMonthId() {
    return referenceMonthId;
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

  public boolean isResizable() {
    return false;
  }

  public int getWidth(){
    return editor.getWidth(); 
  }
}
