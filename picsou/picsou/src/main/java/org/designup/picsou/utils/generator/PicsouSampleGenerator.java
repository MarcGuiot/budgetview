package org.designup.picsou.utils.generator;

import org.crossbowlabs.globs.model.*;
import org.crossbowlabs.globs.model.format.GlobPrinter;
import org.designup.picsou.importer.ofx.OfxExporter;
import org.designup.picsou.model.*;
import static org.designup.picsou.utils.generator.AmountGenerator.*;
import static org.designup.picsou.utils.generator.AmountGenerator.between;
import static org.designup.picsou.utils.generator.CountGenerator.*;
import static org.designup.picsou.utils.generator.DayGenerator.*;
import static org.designup.picsou.utils.generator.DayGenerator.between;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PicsouSampleGenerator {
  private Integer accountId;
  private static final int BANK_ID = 30066;
  private static final int BRANCH_ID = 10674;

  public static void main(String[] args) throws Exception {
    PicsouSampleGenerator generator = new PicsouSampleGenerator();
    generator.init();
    generator.run(200701, 200703);
    generator.write("picsou/dev/samples/sample_small.ofx");
  }

  private GlobRepository repository;
  private List<MonthGenerator> generators = new ArrayList<MonthGenerator>();

  private void init() {

    repository = GlobRepositoryBuilder.init().add(PicsouModel.get().getConstants()).get();

    repository.create(Bank.TYPE, FieldValue.value(Bank.ID, BANK_ID));

    setAccount("23412342234", false);

    add(MasterCategory.INCOME, "WORLDCO SLAVE COMPENSATION 346Z45GF346", once(), between(3500.0, 4500.0), between(1, 5));
    add(MasterCategory.INCOME, "YETANOTHERCO EMPLOY. 233 2A34F2", once(), between(1500.0, 2500.0), between(1, 8));
    add(MasterCategory.HEALTH, "VIR ASS.GENERALES DE FRANCE AGFS", upTo(8), between(5.0, 100.0), any());
    add(MasterCategory.INCOME, "REM CHQ REF10674R04", upTo(5), anyOf(55.0, 35.0, 72.0, 100.0), any());

    add(MasterCategory.MISC_SPENDINGS, "RETRAIT DAB 1805 REF10674A01 CAR", upTo(6), anyOf(-20.0, -40.0, -60.0), any());
    add(new MasterCategory[]{MasterCategory.MISC_SPENDINGS, MasterCategory.FOOD, MasterCategory.TRANSPORTS},
        "RETRAIT DAB 1805 REF10674A01 CAR", upTo(6), anyOf(-20.0, -40.0, -60.0), any());
    add(MasterCategory.TELECOMS, "TIP FRANCE TELECOM MASSY NOR 107", once(), between(-70.0, -100.0), between(1, 5));
    add(MasterCategory.TELECOMS, "PRLV FREE TELECOM FREE HAUTDEBIT", once(), value(-35.5), between(3, 10));
    add(MasterCategory.HOUSE, "PRET IMMO 10674 131234 23", once(), value(-1742.34), any());
    add(MasterCategory.NONE, "CHEQUE 123123", upTo(10), between(-30.0, -150.0), any());
    add(MasterCategory.TELECOMS, "PRLV ORANGE FRANCE SA %s", several(2), between(-35.0, -70.0), between(11, 16));
    add(MasterCategory.MISC_SPENDINGS, "RETRAIT DAB 1805 REF10674A01 CAR", sometimes(), anyOf(-20.0, -40.0, -60.0), any());
    add(MasterCategory.BANK, "ABON FBQ ALERTES, ABON T", once(), between(-1.0, -12.0), between(6, 8));
    add(MasterCategory.HEALTH, "PRLV ASS VIE 15515580008302", once(), value(-85.0), between(6, 8));
    add(MasterCategory.HOUSE, "TIP EDF/GDF CENTRE 04 190225", once(), between(-55.0, -95.0), between(6, 10));
    add(MasterCategory.HOUSE, "TIP GAZ DE FRANCE CENTRE 04 1902", once(), between(-20.0, -50.0), between(6, 10));
    add(MasterCategory.PUERICULTURE, "PRLV C R P A SCOLARITE", once(), value(-217.0), between(12, 15));
    add(MasterCategory.TRANSPORTS, "ECH PRET CAP+IN 10674 101147 06", once(), value(-454.21), between(15, 20));
    add(MasterCategory.TRANSPORTS, "VROUMBOUM ASSURANCES", once(), value(-74.0), between(15, 20));
    add(MasterCategory.BANK, "F COM INTERVENTION DECE 200", sometimes(), between(-4.0, -20.0), between(15, 20));
    add(MasterCategory.TAXES, "PRLV TRESOR PUBLIC 92 IMPOT MENM", once(), value(-334.7), between(15, 20));
    add(MasterCategory.SAVINGS, "ING DIRECT 2134 F324 GDE165", once(), value(-200.0), between(10, 13));

    setAccount("1234234534564567", true);
    add(MasterCategory.HEALTH, "PHARMADISCOUNT SAINT LOUIS", upTo(5), between(-8.0, -60.0), any());
    add(MasterCategory.FOOD, "MONOPRIX SCEAUX", upTo(6), between(-15.0, -150.0), any());
    add(MasterCategory.PUERICULTURE, "TOYS'R'US VELIZY 3802/", sometimes(), between(-15.0, -60.0), any());
    add(MasterCategory.LEISURES, "NATURE ET DECOU VELIZY VILLAC", upTo(2), between(-8.0, -50.0), any());
    add(MasterCategory.HEALTH, "PHIE POMPEI SCEAUX", upTo(5), between(-8.0, -45.0), any());
    add(MasterCategory.FOOD, "MARC SAVIER BALLAINVILLIE", upTo(4), between(-20.0, -100.0), any());
    add(MasterCategory.FOOD, "NESPRESSO FRANC PARIS 17", sometimes(), between(-50.0, -100.0), any());
    add(MasterCategory.CLOTHING, "SOMEWHERE VAD WASQUEHAL", upTo(4), between(-50.0, -150.0), any());
    add(MasterCategory.FOOD, "ATAC MAG SCEAUX", upTo(4), between(-30.0, -100.0), any());
    add(MasterCategory.CLOTHING, "VERT BAUDET VAD TOURCOING", upTo(4), between(-30.0, -100.0), any());
    add(MasterCategory.FOOD, "BIO ANTONY", upTo(6), between(-40.0, -100.0), any());
    add(MasterCategory.LEISURES, "ALAPAGE.COM VILLIERS VPC", sometimes(), between(-15.0, -60.0), any());
    add(MasterCategory.TRANSPORTS, "SARL DELCLERE CHATENAY MALA", upTo(4), anyOf(-15.0, -16.5), any());
    add(MasterCategory.TRANSPORTS, "SARL SIGESS CHATENAY MALA", upTo(3), anyOf(-45.7, -61.0), any());
    add(MasterCategory.TRANSPORTS, "VINCIPARK0921502 ANTONY", sometimes(), between(-1.5, -4.0), any());
    add(MasterCategory.PUERICULTURE, "EVEIL - JEUX ANTONY", upTo(3), between(-10.0, -60.0), any());
    add(MasterCategory.PUERICULTURE, "AUTOUR DE BEBE FRESNES", sometimes(), between(-30.0, -80.0), any());
    add(MasterCategory.HEALTH, "PHIE 4 CHEMINS SCEAUX", upTo(4), between(-5.0, -30.0), any());
    add(MasterCategory.FOOD, "AUCHAN VELIZY CARTE 24371925 PAI", upTo(5), between(-15.0, -150.0), any());
    add(MasterCategory.LEISURES, "AMAZON EU SARL AMAZON EU LUX", sometimes(), between(-8.0, -40.0), any());
    add(MasterCategory.HEALTH, "PHARMADISCOUNT SAINT LOUIS", upTo(3), between(-5.0, -30.0), any());
    add(MasterCategory.CLOTHING, "C - A 01 VELIZY", upTo(3), between(-15.0, -90.0), any());
    add(MasterCategory.FOOD, "PICARD 198 SCEAUX 198/", upTo(3), between(-15.0, -90.0), any());
    add(MasterCategory.HOUSE, "LEROY MERLIN MASSY 324 54235", upTo(3), anyOf(-15.0, -90.0, -50.0, -89.0, -190.0), any());
    add(MasterCategory.HOUSE, "CASTORAMA FRESNES ZRET2", upTo(3), anyOf(-9.5, -65.2, -34.8, -89.0, -119.0), any());
    add(MasterCategory.MULTIMEDIA, "LDLC 34 2456F24", sometimes(), between(-39.0, -200.0), any());
    add(new MasterCategory[]{MasterCategory.MULTIMEDIA, MasterCategory.LEISURES}, "FNAC VELIZY 234RF5", sometimes(), between(-19.0, -200.0), any());
  }

  private void setAccount(String accountNumber, boolean isCardAccount) {
    accountId = repository.create(Account.TYPE,
                                  FieldValue.value(Account.NAME, accountNumber),
                                    FieldValue.value(Account.BANK, BANK_ID),
                                    FieldValue.value(Account.BRANCH_ID, BRANCH_ID),
                                    FieldValue.value(Account.NUMBER, accountNumber),
                                    FieldValue.value(Account.IS_CARD_ACCOUNT, isCardAccount),
                                    FieldValue.value(Account.UPDATE_DATE, new Date()),
                                    FieldValue.value(Account.BALANCE, -1050.12))
            .get(Account.ID);
  }

  private void add(final MasterCategory category,
                   final String label,
                   final CountGenerator count,
                   final AmountGenerator amount,
                   final DayGenerator day) {
    add(new MasterCategory[]{category}, label, count, amount, day);
  }

  private void add(final MasterCategory[] category,
                   final String label,
                   final CountGenerator count,
                   final AmountGenerator amount,
                   final DayGenerator day) {
    generators.add(new MonthGenerator(accountId) {
      public void run(Integer month) {
        int countValue = count.get(month);
        for (int i = 0; i < countValue; i++) {
          Glob transaction = create(month,
                                    day.get(month),
                                    amount.get(),
                                    label,
                                    super.accountId);
          if (category.length == 1) {
            Transaction.setCategory(transaction, category[0].getId(), repository);
          }
          else {
            TransactionToCategory.link(repository, transaction, category);
          }
        }
      }
    });
  }

  private Glob create(Integer month, int day, double amount, String label, int accountId) {
    return repository.create(Transaction.TYPE,
                             FieldValue.value(Transaction.ACCOUNT, accountId),
                               FieldValue.value(Transaction.AMOUNT, amount),
                               FieldValue.value(Transaction.ORIGINAL_LABEL, label),
                               FieldValue.value(Transaction.LABEL, label),
                               FieldValue.value(Transaction.MONTH, month),
                               FieldValue.value(Transaction.DAY, day));
  }

  private void run(int min, int max) {
    repository.enterBulkDispatchingMode();
    for (Integer month : Month.range(min, max)) {
      for (MonthGenerator generator : generators) {
        generator.run(month);
      }
    }
    repository.completeBulkDispatchingMode();
  }

  private void write(String fileName) throws IOException {
    File file = new File(fileName);

    GlobPrinter.print(repository, Account.TYPE);

    System.out.println("Output: " + file.getAbsolutePath());

    FileWriter writer = new FileWriter(file);
    OfxExporter.write(repository, writer);
    writer.close();

  }
}
