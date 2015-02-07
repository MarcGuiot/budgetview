package org.designup.picsou.functests.dashboard;

import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.functests.utils.RestartTestCase;

public class DashboardDatesTest extends RestartTestCase {
  protected String getCurrentDate() {
    return "2015/01/10";
  }

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

  public void testDaysSinceLastManualTransactionInput() throws Exception {
    operations.hideSignposts();

    accounts.createNewAccount()
      .setName("Main")
      .setAccountNumber("000111")
      .selectBank("CIC")
      .setPosition(1000.00)
      .validate();

    dashboard.checkContent("| 0     | Days since your last import                                      |\n" +
                           "| OK    | All your transactions are categorized                            |\n" +
                           "| sunny | No overdraw forecast for your accounts until the end of February |\n" +
                           "| 0     | Available on your main accounts until the end of February        |\n" +
                           "| +1000 | Total amount for your main accounts on 2015/01/01                |");

    transactionCreation.show()
      .shouldUpdatePosition()
      .create(5, "WorldCo", 1000.00)
      .create(10, "Auchan", -50.00);

    dashboard.checkContent("| 0     | Days since your last import                                      |\n" +
                           "| 2     | Transactions to categorize                                       |\n" +
                           "| sunny | No overdraw forecast for your accounts until the end of February |\n" +
                           "| +1950 | Available on your main accounts until the end of February        |\n" +
                           "| +1950 | Total amount for your main accounts on 2015/01/10                |");

    setCurrentDate("2015/02/05");
    operations.changeDate();

    dashboard.checkContent("| 26    | Days since your last import                                   |\n" +
                           "| 2     | Transactions to categorize                                    |\n" +
                           "| sunny | No overdraw forecast for your accounts until the end of March |\n" +
                           "| +1950 | Available on your main accounts until the end of March        |\n" +
                           "| +1950 | Total amount for your main accounts on 2015/01/10             |");

    timeline.selectMonth(201502);
    transactionCreation
      .create(3, "WorldCo", 1000.00)
      .create(4, "FNAC", -100.00);

    dashboard.checkContent("| 1     | Days since your last import                                   |\n" +
                           "| 4     | Transactions to categorize                                    |\n" +
                           "| sunny | No overdraw forecast for your accounts until the end of March |\n" +
                           "| +2850 | Available on your main accounts until the end of March        |\n" +
                           "| +2850 | Total amount for your main accounts on 2015/02/04             |");

    setCurrentDate("2015/02/15");
    operations.changeDate();
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "000222", 500.0, "2015/13/25")
      .addTransaction("2014/12/13", -200.00, "FNAC")
      .load();

    dashboard.checkContent("| 0     | Days since your last import                                   |\n" +
                           "| 5     | Transactions to categorize                                    |\n" +
                           "| sunny | No overdraw forecast for your accounts until the end of March |\n" +
                           "| +3350 | Available on your main accounts until the end of March        |\n" +
                           "| +3350 | Total amount for your main accounts on 2015/02/04             |");
  }

  public void testChangingTheCurrentMonthWhileTheApplicationIsOpen() throws Exception {
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

    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "000234", 1100.0, "2015/01/05")
      .addTransaction("2015/01/01", 1000.00, "WorldCo")
      .addTransaction("2015/01/05", -100.00, "Auchan")
      .load();
    dashboard.checkContent("| 0     | Days since your last import                                      |\n" +
                           "| 3     | Transactions to categorize                                       |\n" +
                           "| sunny | No overdraw forecast for your accounts until the end of February |\n" +
                           "| +1100 | Available on your main accounts until the end of February        |\n" +
                           "| +1100 | Total amount for your main accounts on 2015/01/05                |");

    setCurrentDate("2015/02/05");
    operations.changeDate();

    dashboard.checkContent("| 26    | Days since your last import                                   |\n" +
                           "| 3     | Transactions to categorize                                    |\n" +
                           "| sunny | No overdraw forecast for your accounts until the end of March |\n" +
                           "| +1100 | Available on your main accounts until the end of March        |\n" +
                           "| +1100 | Total amount for your main accounts on 2015/01/05             |");
  }
}
