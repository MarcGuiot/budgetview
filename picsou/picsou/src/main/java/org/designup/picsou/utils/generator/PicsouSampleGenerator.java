package org.designup.picsou.utils.generator;

import org.designup.picsou.importer.ofx.OfxExporter;
import org.designup.picsou.model.*;
import static org.designup.picsou.utils.generator.AmountGenerator.*;
import static org.designup.picsou.utils.generator.CountGenerator.*;
import static org.designup.picsou.utils.generator.DayGenerator.any;
import static org.designup.picsou.utils.generator.DayGenerator.dayBetween;
import org.globsframework.model.FieldValue;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobRepositoryBuilder;
import org.globsframework.model.format.GlobPrinter;

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
    generator.run(200612, 200807);
    generator.write("tmp/sample.ofx");
  }

  private GlobRepository repository;
  private List<MonthGenerator> generators = new ArrayList<MonthGenerator>();

  private void init() {

    repository = GlobRepositoryBuilder.init().add(PicsouModel.get().getConstants()).get();

    repository.create(Bank.TYPE, FieldValue.value(Bank.ID, BANK_ID));

    setAccount("23412342234", false);

    add("WORLDCO SLAVE COMPENSATION 346Z45GF346", once(), between(3500.0, 4500.0), dayBetween(1, 5));
    add("YETANOTHERCO EMPLOY. 233 2A34F2", once(), between(1500.0, 2500.0), dayBetween(1, 8));
    add("VIR ASS.GENERALES DE FRANCE AGFS", upTo(8), between(5.0, 100.0), any());
    add("REM CHQ REF10674R04", upTo(5), anyOf(55.0, 35.0, 72.0, 100.0), any());

    add("RETRAIT DAB 1805 REF10674A01 CAR", upTo(6), anyOf(-20.0, -40.0, -60.0), any());
    add("RETRAIT DAB 1805 REF10674A01 CAR", upTo(6), anyOf(-20.0, -40.0, -60.0), any());
    add("TIP FRANCE TELECOM MASSY NOR 107", once(), between(-70.0, -100.0), dayBetween(1, 5));
    add("PRLV FREE TELECOM FREE HAUTDEBIT", once(), value(-35.5), dayBetween(3, 10));
    add("PRET IMMO 10674 131234 23", once(), value(-1742.34), any());
    add("CHEQUE 123123", upTo(10), between(-30.0, -150.0), any());
    add("PRLV ORANGE FRANCE SA %s", several(2), between(-35.0, -70.0), dayBetween(11, 16));
    add("RETRAIT DAB 1805 REF10674A01 CAR", sometimes(), anyOf(-20.0, -40.0, -60.0), any());
    add("ABON FBQ ALERTES, ABON T", once(), between(-1.0, -12.0), dayBetween(6, 8));
    add("PRLV ASS VIE 15515580008302", once(), value(-85.0), dayBetween(6, 8));
    add("TIP EDF/GDF CENTRE 04 190225", once(), between(-55.0, -95.0), dayBetween(6, 10));
    add("TIP GAZ DE FRANCE CENTRE 04 1902", once(), between(-20.0, -50.0), dayBetween(6, 10));
    add("PRLV C R P A SCOLARITE", once(), value(-217.0), dayBetween(12, 15));
    add("ECH PRET CAP+IN 10674 101147 06", once(), value(-454.21), dayBetween(15, 20));
    add("VROUMBOUM ASSURANCES", once(), value(-74.0), dayBetween(15, 20));
    add("F COM INTERVENTION DECE 200", sometimes(), between(-4.0, -20.0), dayBetween(15, 20));
    add("PRLV TRESOR PUBLIC 92 IMPOT MENM", once(), value(-334.7), dayBetween(15, 20));
    add("ING DIRECT 2134 F324 GDE165", once(), value(-200.0), dayBetween(10, 13));

    setAccount("1234234534564567", true);
    add("PHARMADISCOUNT SAINT LOUIS", upTo(5), between(-8.0, -60.0), any());
    add("MONOPRIX SCEAUX", upTo(6), between(-15.0, -150.0), any());
    add("TOYS'R'US VELIZY 3802/", sometimes(), between(-15.0, -60.0), any());
    add("NATURE ET DECOU VELIZY VILLAC", upTo(2), between(-8.0, -50.0), any());
    add("PHIE POMPEI SCEAUX", upTo(5), between(-8.0, -45.0), any());
    add("MARC SAVIER BALLAINVILLIE", upTo(4), between(-20.0, -100.0), any());
    add("NESPRESSO FRANC PARIS 17", sometimes(), between(-50.0, -100.0), any());
    add("SOMEWHERE VAD WASQUEHAL", upTo(4), between(-50.0, -150.0), any());
    add("ATAC MAG SCEAUX", upTo(4), between(-30.0, -100.0), any());
    add("VERT BAUDET VAD TOURCOING", upTo(4), between(-30.0, -100.0), any());
    add("BIO ANTONY", upTo(6), between(-40.0, -100.0), any());
    add("ALAPAGE.COM VILLIERS VPC", sometimes(), between(-15.0, -60.0), any());
    add("SARL DELCLERE CHATENAY MALA", upTo(4), anyOf(-15.0, -16.5), any());
    add("SARL SIGESS CHATENAY MALA", upTo(3), anyOf(-45.7, -61.0), any());
    add("VINCIPARK0921502 ANTONY", sometimes(), between(-1.5, -4.0), any());
    add("EVEIL - JEUX ANTONY", upTo(3), between(-10.0, -60.0), any());
    add("AUTOUR DE BEBE FRESNES", sometimes(), between(-30.0, -80.0), any());
    add("PHIE 4 CHEMINS SCEAUX", upTo(4), between(-5.0, -30.0), any());
    add("AUCHAN VELIZY CARTE 24371925 PAI", upTo(5), between(-15.0, -150.0), any());
    add("AMAZON EU SARL AMAZON EU LUX", sometimes(), between(-8.0, -40.0), any());
    add("PHARMADISCOUNT SAINT LOUIS", upTo(3), between(-5.0, -30.0), any());
    add("C - A 01 VELIZY", upTo(3), between(-15.0, -90.0), any());
    add("PICARD 198 SCEAUX 198/", upTo(3), between(-15.0, -90.0), any());
    add("LEROY MERLIN MASSY 324 54235", upTo(3), anyOf(-15.0, -90.0, -50.0, -89.0, -190.0), any());
    add("CASTORAMA FRESNES ZRET2", upTo(3), anyOf(-9.5, -65.2, -34.8, -89.0, -119.0), any());
    add("LDLC 34 2456F24", sometimes(), between(-39.0, -200.0), any());
    add("FNAC VELIZY 234RF5", sometimes(), between(-19.0, -200.0), any());
  }

  private void setAccount(String accountNumber, boolean isCardAccount) {
    accountId = repository.create(Account.TYPE,
                                  FieldValue.value(Account.NAME, accountNumber),
                                  FieldValue.value(Account.BANK_ENTITY, BANK_ID),
                                  FieldValue.value(Account.BRANCH_ID, BRANCH_ID),
                                  FieldValue.value(Account.NUMBER, accountNumber),
                                  FieldValue.value(Account.IS_CARD_ACCOUNT, isCardAccount),
                                  FieldValue.value(Account.UPDATE_DATE, new Date()),
                                  FieldValue.value(Account.BALANCE, -1050.12))
      .get(Account.ID);
  }

  private void add(final String label,
                   final CountGenerator count,
                   final AmountGenerator amount,
                   final DayGenerator day) {
    generators.add(new MonthGenerator(accountId) {
      public void run(Integer month) {
        int countValue = count.get(month);
        for (int i = 0; i < countValue; i++) {
          create(month, day.get(month), amount.get(), label, super.accountId);
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
                             FieldValue.value(Transaction.DAY, day),
                             FieldValue.value(Transaction.BANK_MONTH, month),
                             FieldValue.value(Transaction.BANK_DAY, day));
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
