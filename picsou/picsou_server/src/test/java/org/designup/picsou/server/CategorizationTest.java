package org.designup.picsou.server;

import org.designup.picsou.functests.checkers.CategoryChooserChecker;
import org.designup.picsou.functests.checkers.DataChecker;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.transactions.TransactionView;
import org.designup.picsou.model.Category;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.Transaction;
import org.globsframework.model.Glob;
import org.globsframework.utils.Files;
import org.globsframework.utils.TestUtils;
import org.uispec4j.Table;
import org.uispec4j.TableCellValueConverter;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

import java.awt.*;

public class CategorizationTest extends ServerFuncTestCase {

  public void test() throws Exception {
    String fileName = TestUtils.getFileName(this, ".qif");

    Files.copyStreamTofile(CategorizationTest.class.getResourceAsStream(PICSOU_DEV_TESTFILES_SG1_QIF),
                           fileName);
    createAndLogUser("user", "_passd1", fileName);

    Table transactionTable = window.getTable(Transaction.TYPE.getName());
    WindowInterceptor.init(transactionTable.editCell(2, TransactionView.CATEGORY_COLUMN_INDEX)
      .getButton().triggerClick())
      .process(new WindowHandler() {
        public Trigger process(final Window window) throws Exception {
          return new Trigger() {
            public void run() throws Exception {
              CategoryChooserChecker.selectCategory(window, DataChecker.getCategoryName(MasterCategory.FOOD));
            }
          };
        }
      }).run();
    checkTransactionChange();
    window.dispose();
    window = WindowInterceptor.run(new Trigger() {
      public void run() throws Exception {
        PicsouApplication.main();
      }
    });
  }

  private void checkTransactionChange() {
    Table categoryTable = window.getTable("category");
    MasterCategory category = MasterCategory.FOOD;
    int rowIndex = getCellRowIndex(categoryTable, category);
    assertTrue(categoryTable.cellEquals(rowIndex, 2, "-18"));
  }

  private int getCellRowIndex(Table categoryTable, MasterCategory category) {
    int rowCount = categoryTable.getRowCount();
    while (rowCount > 0) {
      rowCount--;
      Object o = categoryTable.getContentAt(rowCount, 0, new TableCellValueConverter() {
        public Object getValue(int i, int i1, Component component, Object object) {
          return object;
        }
      });
      if (((Glob)o).get(Category.ID).equals(category.getId())) {
        return rowCount;
      }
    }
    fail(DataChecker.getCategoryName(category) + " not found");
    return -1;
  }
}
