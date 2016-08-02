package com.budgetview.desktop.analysis.table;

import com.budgetview.desktop.analysis.SeriesChartsColors;
import com.budgetview.desktop.series.view.SeriesWrapperType;
import org.globsframework.gui.views.GlobTableColumn;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.utils.directory.Directory;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.util.Comparator;

public class SeriesEvolutionLabelColumn implements GlobTableColumn {

  private SeriesEvolutionLabelEditor editor;

  public SeriesEvolutionLabelColumn(GlobTableView view,
                                    GlobRepository repository, Directory directory,
                                    SeriesChartsColors colors) {
    this.editor = new SeriesEvolutionLabelEditor(view, repository, directory, colors);
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

  public void setReferenceMonthId(Integer referenceMonthId) {
    editor.setReferenceMonth(referenceMonthId);
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
}