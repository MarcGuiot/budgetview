package org.designup.picsou.functests.dashboard;

import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.functests.utils.RestartTestCase;
import org.designup.picsou.gui.model.AccountStat;
import org.designup.picsou.model.Account;
import org.globsframework.model.format.GlobPrinter;

public class DashboardDatesTest extends RestartTestCase {
  protected String getCurrentDate() {
    return "2015/01/10";
  }

  //  fail("positions de comptes: quelle position/date choisir dans Account? Mise à jour en cas de saisie manuelle ?");
//  fail("changement de mois avec appli ouverte");
  public void testDaysSinceLastImport() throws Exception {
    operations.hideSignposts();
    dashboard.checkContent("| 0     | Days since your last import                                     |\n" +
                           "| OK    | All your transactions are categorized                           |\n" +
                           "| sunny | No overdraw forecast for your accounts until the end of January |\n" +
                           "| 0     | Available on your main accounts until the end of January        |\n" +
                           "| 0     | Total amount for your main accounts on 2015/01/10               |");

    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "000123", 250.0, "2015/01/10")
      .addTransaction("2015/01/10", -100.00, "Auchan")
      .addTransaction("2015/01/05", -200.00, "FNAC")
      .addTransaction("2015/01/01", 3000.00, "WorldCo")
      .load();

    setCurrentDate("2015/01/15");
    restartApplication();

    dashboard.checkContent("| 5     | Days since your last import                                      |\n" +
                           "| 3     | Transactions to categorize                                       |\n" +
                           "| sunny | No overdraw forecast for your accounts until the end of February |\n" +
                           "| +250  | Available on your main accounts until the end of February        |\n" +
                           "| +250  | Total amount for your main accounts on 2015/01/10                |");

    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "000123", 200.0, "2015/01/15")
      .addTransaction("2015/01/15", -50.00, "Auchan")
      .load();

    dashboard.checkContent("| 0     | Days since your last import                                      |\n" +
                           "| 4     | Transactions to categorize                                       |\n" +
                           "| sunny | No overdraw forecast for your accounts until the end of February |\n" +
                           "| +200  | Available on your main accounts until the end of February        |\n" +
                           "| +200  | Total amount for your main accounts on 2015/01/15                |");

    setCurrentDate("2015/01/25");
    operations.changeDate();

    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "000234", 200.0, "2015/01/25")
      .addTransaction("2015/01/25", -50.00, "Auchan")
      .load();

    dashboard.checkContent("| 0     | Days since your last import                                      |\n" +
                           "| 5     | Transactions to categorize                                       |\n" +
                           "| sunny | No overdraw forecast for your accounts until the end of February |\n" +
                           "| +400  | Available on your main accounts until the end of February        |\n" +
                           "| +400  | Total amount for your main accounts on 2015/01/25                |");

    setCurrentDate("2015/02/05");
    operations.changeDate();
    restartApplication();

    timeline.checkDisplays("2015/01", "2015/02");
    dashboard.checkContent("| 11    | Days since your last import                                   |\n" +
                           "| 5     | Transactions to categorize                                    |\n" +
                           "| sunny | No overdraw forecast for your accounts until the end of March |\n" +
                           "| +400  | Available on your main accounts until the end of March        |\n" +
                           "| +400  | Total amount for your main accounts on 2015/01/25             |");

    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "000234", 200.0, "2015/01/31")
      .addTransaction("2015/01/31", -100.00, "Auchan")
      .load();

    dashboard.checkContent("| 0     | Days since your last import                                   |\n" +
                           "| 6     | Transactions to categorize                                    |\n" +
                           "| sunny | No overdraw forecast for your accounts until the end of March |\n" +
                           "| +300  | Available on your main accounts until the end of March        |\n" +
                           "| +300  | Total amount for your main accounts on 2015/01/31             |");
  }

  public void testInformationShownWhenLastImportIsInThePreviousMonth() throws Exception {
    operations.hideSignposts();
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "000234", 200.0, "2014/12/25")
      .addTransaction("2014/12/20", -50.00, "Auchan")
      .load();

    dashboard.checkContent("| 0     | Days since your last import                                      |\n" +
                           "| 1     | Transactions to categorize                                       |\n" +
                           "| sunny | No overdraw forecast for your accounts until the end of February |\n" +
                           "| +200  | Available on your main accounts until the end of February        |\n" +
                           "| +200  | Total amount for your main accounts on 2014/12/20                |");
  }
}
