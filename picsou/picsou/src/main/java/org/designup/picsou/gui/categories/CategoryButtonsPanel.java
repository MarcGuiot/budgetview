package org.designup.picsou.gui.categories;

import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.gui.utils.Icons;
import org.designup.picsou.model.Category;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.Glob;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CategoryButtonsPanel {
  public static final String BUTTON_PANEL = "BUTTONS";
  public static final String HOLE_PANEL = "HOLE";

  private JPanel panel;
  private JLabel categoryLabel;
  private JPanel buttonPanel;
  private CardLayout cardLayout;

  public CategoryButtonsPanel(final Glob category,
                              final JLabel categoryLabel,
                              final JPanel parentPanel,
                              AbstractAction addCategoryAction,
                              AbstractAction renameCategoryAction,
                              AbstractAction deleteCategoryAction,
                              final SelectionService selectionService) {
    this.categoryLabel = categoryLabel;

    buttonPanel = Gui.createHorizontalBoxLayoutPanel();
    final JPanel holePanel = Gui.createHorizontalBoxLayoutPanel();
    panel = new JPanel();
    panel.setOpaque(false);
    cardLayout = new CardLayout();
    panel.setLayout(cardLayout);

    JButton renameCategoryButton = createRenameCategoryButton(renameCategoryAction);
    JButton addCategoryButton = createAddCategoryButton(addCategoryAction);
    JButton deleteCategoryButton = createDeleteCategoryButton(deleteCategoryAction);
    if (!Category.isMaster(category)) {
      buttonPanel.add(renameCategoryButton);
      configureButtonPanelEnabling(renameCategoryButton);
      holePanel.add(Box.createRigidArea(new Dimension(13, 13)));
    }
    if (Category.isMaster(category) && (!Category.isReserved(category)) && (!Category.isSystem(category))) {
      buttonPanel.add(addCategoryButton);
      configureButtonPanelEnabling(addCategoryButton);
      holePanel.add(Box.createRigidArea(new Dimension(13, 13)));
    }
    if (!Category.isMaster(category) && !Category.isSystem(category)) {
      buttonPanel.add(deleteCategoryButton);
      configureButtonPanelEnabling(deleteCategoryButton);
      holePanel.add(Box.createRigidArea(new Dimension(13, 13)));
    }

    panel.add(buttonPanel, BUTTON_PANEL);
    panel.add(holePanel, HOLE_PANEL);
    cardLayout.show(panel, HOLE_PANEL);

    panel.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        selectionService.select(category);
      }
    });

    buttonPanel.addMouseListener(new MouseAdapter() {
      public void mouseExited(MouseEvent e) {
        enableButtons(HOLE_PANEL);
      }
    });

    parentPanel.addMouseListener(new MouseAdapter() {
      public void mouseExited(MouseEvent e) {
        if (!isMouseInButtons(convertMouseLocation(e, parentPanel))) {
          enableButtons(HOLE_PANEL);
        }
      }
    });

    categoryLabel.addMouseListener(new MouseAdapter() {
      public void mouseEntered(MouseEvent e) {
        enableButtons(BUTTON_PANEL);
      }

      public void mouseExited(MouseEvent e) {
        if (panel.isVisible() && !isMouseInButtons(convertMouseLocation(e, categoryLabel))) {
          enableButtons(HOLE_PANEL);
        }
      }
    });
  }

  public JPanel getPanel() {
    return panel;
  }

  private void configureButtonPanelEnabling(final JButton button) {
    button.addMouseListener(new MouseAdapter() {
      public void mouseReleased(MouseEvent e) {
        enableButtons(HOLE_PANEL);
      }

      public void mouseExited(MouseEvent e) {
        if (!isMouseInButtons(convertMouseLocation(e, button))) {
          enableButtons(HOLE_PANEL);
        }
      }
    });
  }

  private Point convertMouseLocation(MouseEvent e, JComponent component) {
    Point mouseOnScreen = new Point(e.getPoint());
    SwingUtilities.convertPointToScreen(mouseOnScreen, component);
    return mouseOnScreen;
  }

  public void enableButtons(String cardName) {
    cardLayout.show(panel, cardName);
  }

  private boolean isMouseInButtons(Point mouseOnScreen) {
    if (!categoryLabel.isVisible()) {
      return false;
    }
    if (!buttonPanel.isVisible()) {
      return false;
    }
    Point buttonLocation = buttonPanel.getLocationOnScreen();
    Point labelLocation = categoryLabel.getLocationOnScreen();
    return ((mouseOnScreen.getY() > buttonLocation.getY()) &&
            (mouseOnScreen.getY() < buttonLocation.getY() + buttonPanel.getHeight()) &&
            (mouseOnScreen.getX() > labelLocation.getX() + categoryLabel.getWidth() - 1) &&
            (mouseOnScreen.getX() < buttonLocation.getX() + buttonPanel.getWidth()));
  }

  private JButton createAddCategoryButton(AbstractAction addCategoryAction) {
    final JButton addCategoryButton = new JButton(addCategoryAction);
    configureButton(addCategoryButton, Icons.ADD_ICON, Icons.ADD_ROLLOVER_ICON, "Add");
    addCategoryButton.setToolTipText(Lang.get("category.create.tooltip"));
    return addCategoryButton;
  }

  private JButton createDeleteCategoryButton(AbstractAction deleteCategoryAction) {
    final JButton deleteCategoryButton = new JButton(deleteCategoryAction);
    configureButton(deleteCategoryButton, Icons.DELETE_ICON, Icons.DELETE_ROLLOVER_ICON, "Delete");
    deleteCategoryButton.setToolTipText(Lang.get("category.delete.tooltip"));
    return deleteCategoryButton;
  }

  private JButton createRenameCategoryButton(AbstractAction renameCategoryAction) {
    final JButton renameCategoryButton = new JButton(renameCategoryAction);
    configureButton(renameCategoryButton, Icons.RENAME_ICON, Icons.RENAME_ROLLOVER_ICON, "Rename");
    renameCategoryButton.setToolTipText(Lang.get("category.rename.tooltip"));
    return renameCategoryButton;
  }

  private void configureButton(JButton button, Icon icon, Icon rolloverIcon, String name) {
    Gui.setIcons(button, icon, rolloverIcon, rolloverIcon);
    Gui.configureIconButton(button, name, new Dimension(13, 13));
  }
}
