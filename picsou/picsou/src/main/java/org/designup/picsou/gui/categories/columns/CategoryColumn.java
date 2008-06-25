package org.designup.picsou.gui.categories.columns;

import org.designup.picsou.gui.categories.actions.CreateCategoryAction;
import org.designup.picsou.gui.categories.actions.DeleteCategoryAction;
import org.designup.picsou.gui.categories.actions.RenameCategoryAction;
import org.designup.picsou.gui.components.AbstractRolloverEditor;
import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.model.Category;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.layout.Anchor;
import org.globsframework.gui.splits.layout.Fill;
import org.globsframework.gui.splits.layout.GridBagBuilder;
import org.globsframework.gui.splits.utils.TransparentIcon;
import org.globsframework.gui.views.CellPainter;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.plaf.PanelUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CategoryColumn extends AbstractRolloverEditor {
  private static final TransparentIcon SUBCATEGORY_MARGIN_ICON = new TransparentIcon(10, 5);

  private CreateCategoryAction addCategoryAction;
  private RenameCategoryAction renameCategoryAction;
  private DeleteCategoryAction deleteCategoryAction;
  private GlobStringifier categoryStringifier;
  private CategoryLabelCustomizer customizer;
  private CategoryBackgroundPainter backgroundPainter;

  public CategoryColumn(CategoryLabelCustomizer customizer, CategoryBackgroundPainter painter, GlobTableView view,
                        DescriptionService descriptionService, GlobRepository repository, final Directory directory) {
    super(view, descriptionService, repository, directory);
    this.customizer = customizer;
    this.backgroundPainter = painter;
    addCategoryAction = new CreateCategoryAction(repository, directory) {

      public JDialog getDialog(ActionEvent e) {
        return PicsouDialog.create(directory.get(JFrame.class));
      }
    };
    renameCategoryAction = new RenameCategoryAction(repository, directory) {
      public JDialog getDialog(ActionEvent e) {
        return PicsouDialog.create(directory.get(JFrame.class));
      }
    };
    deleteCategoryAction = new DeleteCategoryAction(repository, directory);
    categoryStringifier = descriptionService.getStringifier(Category.TYPE);
  }

  protected Component getComponent(final Glob category, boolean render) {
    JPanel panel = new JPanel();
    panel.setOpaque(false);
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

    JLabel label = addCategoryLabel(category, panel);
    panel.add(Box.createRigidArea(new Dimension(2, 0)));

    final CategoryButtonsPanel buttonsPanel = new CategoryButtonsPanel(category,
                                                                       label,
                                                                       panel,
                                                                       createActionWrapper(category, addCategoryAction),
                                                                       createActionWrapper(category, renameCategoryAction),
                                                                       createActionWrapper(category, deleteCategoryAction),
                                                                       selectionService);
    panel.add(buttonsPanel.getPanel());

    JPanel fillPanel = new JPanel();
    fillPanel.setOpaque(false);

    GridBagBuilder builder = GridBagBuilder.init().setOpaque(false);
    builder.add(panel, 0, 0, 1, 1, 0, 1, Fill.NONE, Anchor.CENTER, new Insets(0, 0, 0, 0));
    builder.add(fillPanel, 1, 0, 1, 1, 1, 1, Fill.HORIZONTAL, Anchor.CENTER, new Insets(0, 0, 0, 0));
    JPanel mainPanel = builder.getPanel();
    paintBackground(mainPanel, category);
    return mainPanel;
  }

  private void paintBackground(JPanel panel, Glob category) {
    panel.setUI(new PainterUI(backgroundPainter, category, row, column, isSelected, hasFocus));
  }

  private ActionWrapper createActionWrapper(Glob category, AbstractAction action) {
    return new ActionWrapper(selectionService, action, category);
  }

  private JLabel addCategoryLabel(final Glob category, JPanel panel) {
    String categoryToDisplay = categoryStringifier.toString(category, repository);
    JLabel label = new JLabel(categoryToDisplay);
    customizer.process(label, category, isSelected, hasFocus, row, column);
    label.setIcon(Category.isMaster(category) ? null : SUBCATEGORY_MARGIN_ICON);
    label.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        selectionService.select(category);
        stopCellEditing();
      }
    });

    panel.add(label);
    return label;
  }

  private class PainterUI extends PanelUI {
    private CellPainter cellPainter;
    private Glob category;
    private int row;
    private int column;
    private boolean selected;
    private boolean hasFocus;

    public PainterUI(CellPainter cellPainter, Glob category, int row, int column, boolean selected, boolean hasFocus) {
      this.cellPainter = cellPainter;
      this.category = category;
      this.row = row;
      this.column = column;
      this.selected = selected;
      this.hasFocus = hasFocus;
    }

    public void paint(Graphics g, JComponent c) {
      cellPainter.paint(g, category, row, column, selected, hasFocus, c.getWidth(), c.getHeight());
      super.paint(g, c);
    }
  }

  private class ActionWrapper extends AbstractAction {
    private SelectionService selectionService;
    private AbstractAction action;
    private Glob category;

    public ActionWrapper(SelectionService selectionService, AbstractAction action, Glob category) {
      this.selectionService = selectionService;
      this.action = action;
      this.category = category;
    }

    public void actionPerformed(ActionEvent event) {
      tableView.getComponent().requestFocus();
      selectionService.select(category);
      action.actionPerformed(event);
    }
  }
}
