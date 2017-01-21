package com.budgetview.server.cloud.persistence;

import com.budgetview.server.cloud.model.ProviderAccount;
import com.budgetview.server.cloud.model.ProviderTransaction;
import com.budgetview.server.config.ConfigService;
import com.budgetview.shared.cloud.budgea.BudgeaCategory;
import com.budgetview.shared.model.DefaultSeries;
import com.budgetview.shared.model.Provider;
import junit.framework.TestCase;
import org.globsframework.model.*;
import org.globsframework.utils.Dates;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import static org.globsframework.model.FieldValue.value;

public class CloudSerializerTest extends TestCase {
  public void test() throws Exception {

    ConfigService configService = new ConfigService("budgetview/bv_server/dev/config/bv_cloud_test.properties");
    Directory directory = new DefaultDirectory();
    directory.add(configService);
    CloudSerializer serializer = new CloudSerializer(directory);

    FieldValue[] accountValues = {value(ProviderAccount.ID, 1),
      value(ProviderAccount.NAME, "My account"),
      value(ProviderAccount.NUMBER, "123456789"),
      value(ProviderAccount.PROVIDER, Provider.BUDGEA.getId()),
      value(ProviderAccount.PROVIDER_CONNECTION, 3),
      value(ProviderAccount.PROVIDER_BANK_NAME, "CIC"),
      value(ProviderAccount.PROVIDER_BANK_ID, 42),
      value(ProviderAccount.ACCOUNT_TYPE, "Chèque"),
      value(ProviderAccount.DELETED, false),
      value(ProviderAccount.POSITION, 1000.00),
      value(ProviderAccount.POSITION_MONTH, 201609),
      value(ProviderAccount.POSITION_DAY, 3)};

    FieldValue[] transactionValues = {
      value(ProviderTransaction.ID, 1),
      value(ProviderTransaction.ACCOUNT, 1),
      value(ProviderTransaction.LABEL, "FNAC"),
      value(ProviderTransaction.ORIGINAL_LABEL, "ACHAT CB FNAC"),
      value(ProviderTransaction.AMOUNT, -50.00),
      value(ProviderTransaction.OPERATION_DATE, Dates.parse("2016/09/02")),
      value(ProviderTransaction.BANK_DATE, Dates.parse("2016/09/03")),
      value(ProviderTransaction.DEFAULT_SERIES_ID, DefaultSeries.ELECTRICITY.getId()),
      value(ProviderTransaction.PROVIDER_CATEGORY_NAME, BudgeaCategory.ELECTRICITE.getName()),
      value(ProviderTransaction.DELETED, false)};

    GlobRepository sourceRepository = GlobRepositoryBuilder.createEmpty();
    sourceRepository.create(ProviderAccount.TYPE, accountValues);
    sourceRepository.create(ProviderTransaction.TYPE, transactionValues);

    byte[] bytes = serializer.toBlob(sourceRepository);

    GlobRepository targetRepository = GlobRepositoryBuilder.createEmpty();
    serializer.readBlob(bytes, targetRepository);

    GlobRepositoryChecker checker = new GlobRepositoryChecker(targetRepository);
    Glob account = targetRepository.get(Key.create(ProviderAccount.TYPE, 1));
    checker.checkFields(account, accountValues);
    Glob transaction = targetRepository.get(Key.create(ProviderTransaction.TYPE, 1));
    checker.checkFields(transaction, transactionValues);
  }
}