package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;

public class AllocationLearningTest extends LoggedInFunctionalTestCase {

  public void testLearning() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2006/01/10", -1.0, "Menu K")
      .load();
    transactions.assignCategory(MasterCategory.FOOD, 0);

    OfxBuilder.init(this)
      .addTransaction("2006/01/11", -2.0, "Menu K")
      .load();
    transactions
      .initContent()
      .add("11/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -2.0, MasterCategory.FOOD)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.0, MasterCategory.FOOD)
      .check();
  }

  public void testLearningForSubcategories() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2006/01/10", -1.0, "Menu K")
      .load();

    categories.createSubCategory(MasterCategory.FOOD, "Apero");
    categories.select(MasterCategory.ALL);
    transactions.assignCategory("Apero", 0);

    OfxBuilder.init(this)
      .addTransaction("2006/01/11", -2.0, "Menu K")
      .load();
    transactions
      .initContent()
      .add("11/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -2.0, "Apero")
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.0, "Apero")
      .check();
  }

  public void testDeleteSubCategoryMoveLearningOnParent() throws Exception {
    categories.createSubCategory(MasterCategory.FOOD, "Apero");
    OfxBuilder
      .init(this)
      .addCategory(MasterCategory.FOOD, "Apero")
      .addTransaction("2006/01/10", -1.0, "Chez Lulu")
      .load();

    categories.select(MasterCategory.ALL);
    transactions.assignCategory("Apero", 0);

    categories.select("Apero");

    transactions
      .initContent()
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Chez Lulu", "", -1.0, "Apero")
      .check();

    categories.deleteSelected();

    categories.assertCategoryNotFound("Apero");

    categories.assertSelectionEquals(MasterCategory.FOOD);

    OfxBuilder
      .init(this)
      .addTransaction("2006/01/15", -2.0, "Chez Lulu")
      .load();

    transactions
      .initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Chez Lulu", "", -2.0, MasterCategory.FOOD)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Chez Lulu", "", -1.0, MasterCategory.FOOD)
      .check();

  }

  public void testNoLearningForChecks() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2006/01/10", -1.0, "Cheque 123")
      .load();
    transactions.assignCategory(MasterCategory.FOOD, 0);

    OfxBuilder.init(this)
      .addTransaction("2006/01/11", -2.0, "Cheque 123")
      .load();
    transactions
      .initContent()
      .add("11/01/2006", TransactionType.CHECK, "CHEQUE N. 123", "", -2.0)
      .add("10/01/2006", TransactionType.CHECK, "CHEQUE N. 123", "", -1.0, MasterCategory.FOOD)
      .check();
  }

  public void testNoLearningForLabelWithNumberOnly() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2006/01/10", -1.0, "123")
      .load();
    transactions.assignCategory(MasterCategory.FOOD, 0);

    OfxBuilder.init(this)
      .addTransaction("2006/01/11", -2.0, "123")
      .load();
    transactions
      .initContent()
      .add("11/01/2006", TransactionType.PRELEVEMENT, "123", "", -2.0)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "123", "", -1.0, MasterCategory.FOOD)
      .check();
  }

  public void testNoPropagationForLabelWithNumberOnly() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2006/01/10", -1.0, "123")
      .addTransaction("2006/01/11", -2.0, "123")
      .load();
    transactions.assignCategory(MasterCategory.FOOD, 0);

    transactions
      .initContent()
      .add("11/01/2006", TransactionType.PRELEVEMENT, "123", "", -2.0, MasterCategory.FOOD)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "123", "", -1.0)
      .check();
  }

  public void testLearningWithAmbiguity() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.0, "Menu K")
      .load();
    transactions.assignCategory(MasterCategory.FOOD, 0);
    transactions
      .initContent()
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.0, MasterCategory.FOOD)
      .check();

    OfxBuilder
      .init(this)
      .addTransaction("2006/01/11", -1.0, "Menu K")
      .load();
    transactions.assignCategory(MasterCategory.HEALTH, 0);
    transactions
      .initContent()
      .add("11/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.0, MasterCategory.HEALTH)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.0, MasterCategory.FOOD)
      .check();

    OfxBuilder
      .init(this)
      .addTransaction("2006/01/12", -1.0, "Menu K")
      .load();
    transactions
      .initContent()
      .add("12/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.0, MasterCategory.FOOD, MasterCategory.HEALTH)
      .add("11/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.0, MasterCategory.HEALTH)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.0, MasterCategory.FOOD)
      .check();
  }

  public void testUnlearn() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.0, "Menu K")
      .addTransaction("2006/01/11", -1.0, "Menu K")
      .load();
    transactions.assignCategory(MasterCategory.FOOD, 0);
    transactions
      .initContent()
      .add("11/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.0, MasterCategory.FOOD)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.0, MasterCategory.FOOD)
      .check();

    transactions.assignCategory(MasterCategory.NONE, 0);
    transactions
      .initContent()
      .add("11/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.0)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.0, MasterCategory.FOOD)
      .check();

    OfxBuilder
      .init(this)
      .addTransaction("2006/01/12", -1.0, "Menu K")
      .load();
    transactions
      .initContent()
      .add("12/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.0, MasterCategory.FOOD)
      .add("11/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.0)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.0, MasterCategory.FOOD)
      .check();

    transactions.assignCategory(MasterCategory.NONE, 0, 2);
    transactions
      .initContent()
      .add("12/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.0)
      .add("11/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.0)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.0)
      .check();

    OfxBuilder
      .init(this)
      .addTransaction("2006/01/13", -1.0, "Menu K")
      .load();
    transactions
      .initContent()
      .add("13/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.0)
      .add("12/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.0)
      .add("11/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.0)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.0)
      .check();
  }

  public void testlearnUnlearnLearnTaxes_LearnIfCategoryIsNullOrSetToNONE() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.0, "TIP   4543634 IMPOT CEL OV34Z 4365345 65456")
      .addTransaction("2006/01/13", -2.0, "TIP   4543634 IMPOT CEL MT34DE 4365345 65456")
      .load();
    transactions.assignCategory(MasterCategory.TAXES, 0);
    transactions
      .initContent()
      .add("13/01/2006", TransactionType.PRELEVEMENT, "TIP   4543634 IMPOT CEL MT34DE 4365345 65456",
           "", -2.0, MasterCategory.TAXES)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "TIP   4543634 IMPOT CEL OV34Z 4365345 65456",
           "", -1.0, MasterCategory.TAXES)
      .check();
    transactions.assignCategory(MasterCategory.NONE, 0, 1);
    transactions.assignCategory(MasterCategory.TAXES, 0);
    transactions
      .initContent()
      .add("13/01/2006", TransactionType.PRELEVEMENT, "TIP   4543634 IMPOT CEL MT34DE 4365345 65456",
           "", -2.0, MasterCategory.TAXES)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "TIP   4543634 IMPOT CEL OV34Z 4365345 65456",
           "", -1.0, MasterCategory.TAXES)
      .check();

  }
}
