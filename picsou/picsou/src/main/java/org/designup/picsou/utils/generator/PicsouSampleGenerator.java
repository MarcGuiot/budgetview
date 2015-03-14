package org.designup.picsou.utils.generator;

import org.designup.picsou.exporter.ofx.OfxExporter;
import org.designup.picsou.model.*;
import static org.designup.picsou.utils.generator.AmountGenerator.*;
import static org.designup.picsou.utils.generator.CountGenerator.*;
import static org.designup.picsou.utils.generator.DayGenerator.*;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobRepositoryBuilder;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.utils.Dates;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PicsouSampleGenerator {
  private Integer accountId;
  private static final int BANK_ID = 30066;
  private static final int BRANCH_ID = 10674;

  private static final String UPDATE_DATE = "2008/10/31";

  public static void main(String[] args) throws Exception {
    PicsouSampleGenerator generator = new PicsouSampleGenerator();
    generator.run(200809, 200810);
    generator.write("tmp/sample.ofx");
  }

  private GlobRepository repository;
  private List<MonthGenerator> generators = new ArrayList<MonthGenerator>();

  public PicsouSampleGenerator() {
    this(GlobRepositoryBuilder.init().add(PicsouModel.get().getConstants()).get());
  }

  public PicsouSampleGenerator(GlobRepository repository) {
    this.repository = repository;
    init();
  }

  private void init() {

    repository.create(Bank.TYPE, value(Bank.ID, BANK_ID));

    setAccount("23412342234", false, UPDATE_DATE);

    add("WORLDCO SLAVE COMPENSATION 346Z45GF346", once(), between(3500.0, 4500.0), dayBetween(1, 5));
    add("YETANOTHERCO EMPLOY. 233 2A34F2", once(), between(1500.0, 2500.0), dayBetween(1, 8));
    add("VIR ASS.GENERALES DE FRANCE AGFS", upTo(8), between(5.0, 100.0), anyDay());
    add("REM CHQ REF10674R04", upTo(5), anyOf(55.0, 35.0, 72.0, 100.0), anyDay());

    add("RETRAIT DAB 1805 REF10674A01 CAR", upTo(6), anyOf(-20.0, -40.0, -60.0), anyDay());
    add("RETRAIT DAB 1805 REF10674A01 CAR", upTo(6), anyOf(-20.0, -40.0, -60.0), anyDay());
    add("TIP FRANCE TELECOM MASSY NOR 107", once(), between(-70.0, -100.0), dayBetween(1, 5));
    add("PRLV FREE TELECOM FREE HAUTDEBIT", once(), fixedValue(-35.5), dayBetween(3, 10));
    add("PRET IMMO 10674 131234 23", once(), fixedValue(-1742.34), anyDay());
    add("CHEQUE 123123", upTo(10), between(-30.0, -150.0), anyDay());
    add("PRLV ORANGE FRANCE SA %s", several(2), between(-35.0, -70.0), dayBetween(11, 16));
    add("RETRAIT DAB 1805 REF10674A01 CAR", sometimes(), anyOf(-20.0, -40.0, -60.0), anyDay());
    add("ABON FBQ ALERTES, ABON T", once(), between(-1.0, -12.0), dayBetween(6, 8));
    add("PRLV ASS VIE 15515580008302", once(), fixedValue(-85.0), dayBetween(6, 8));
    add("TIP EDF/GDF CENTRE 04 190225", once(), between(-55.0, -95.0), dayBetween(6, 10));
    add("TIP GAZ DE FRANCE CENTRE 04 1902", once(), between(-20.0, -50.0), dayBetween(6, 10));
    add("PRLV C R P A SCOLARITE", once(), fixedValue(-217.0), dayBetween(12, 15));
    add("ECH PRET CAP+IN 10674 101147 06", once(), fixedValue(-454.21), dayBetween(15, 20));
    add("VROUMBOUM ASSURANCES", once(), fixedValue(-74.0), dayBetween(15, 20));
    add("F COM INTERVENTION DECE 200", sometimes(), between(-4.0, -20.0), dayBetween(15, 20));
    add("PRLV TRESOR PUBLIC 92 IMPOT MENM", once(), fixedValue(-334.7), dayBetween(15, 20));
    add("ING DIRECT 2134 F324 GDE165", once(), fixedValue(-200.0), dayBetween(10, 13));

    setAccount("1234234534564567", true, UPDATE_DATE);
    add("PHARMADISCOUNT SAINT LOUIS", upTo(5), between(-8.0, -60.0), anyDay());
    add("MONOPRIX SCEAUX", upTo(6), between(-15.0, -150.0), anyDay());
    add("TOYS'R'US VELIZY 3802/", sometimes(), between(-15.0, -60.0), anyDay());
    add("NATURE ET DECOU VELIZY VILLAC", upTo(2), between(-8.0, -50.0), anyDay());
    add("PHIE POMPEI SCEAUX", upTo(5), between(-8.0, -45.0), anyDay());
    add("MARC SAVIER BALLAINVILLIE", upTo(4), between(-20.0, -100.0), anyDay());
    add("NESPRESSO FRANC PARIS 17", sometimes(), between(-50.0, -100.0), anyDay());
    add("SOMEWHERE VAD WASQUEHAL", upTo(4), between(-50.0, -150.0), anyDay());
    add("ATAC MAG 123 SCEAUX", upTo(2), between(-30.0, -100.0), anyDay());
    add("ATAC MAG 321 SCEAUX", upTo(2), between(-30.0, -100.0), anyDay());
    add("VERT BAUDET VAD TOURCOING", upTo(4), between(-30.0, -100.0), anyDay());
    add("BIO ANTONY", upTo(6), between(-40.0, -100.0), anyDay());
    add("ALAPAGE.COM VILLIERS VPC", sometimes(), between(-15.0, -60.0), anyDay());
    add("SARL DELCLERE CHATENAY MALA", upTo(4), anyOf(-15.0, -16.5), anyDay());
    add("SARL SIGESS CHATENAY MALA", upTo(3), anyOf(-45.7, -61.0), anyDay());
    add("VINCIPARK0921502 ANTONY", sometimes(), between(-1.5, -4.0), anyDay());
    add("EVEIL - JEUX ANTONY", upTo(3), between(-10.0, -60.0), anyDay());
    add("AUTOUR DE BEBE FRESNES", sometimes(), between(-30.0, -80.0), anyDay());
    add("PHARMA 4 CHEMINS", upTo(4), between(-5.0, -30.0), anyDay());
    add("AUCHAN VELIZY CARTE 24371925 PAI", upTo(5), between(-15.0, -150.0), anyDay());
    add("AMAZON EU SARL AMAZON EU LUX", sometimes(), between(-8.0, -40.0), anyDay());
    add("PHARMADISCOUNT SAINT LOUIS", upTo(3), between(-5.0, -30.0), anyDay());
    add("C - A 01 VELIZY", upTo(3), between(-15.0, -90.0), anyDay());
    add("PICARD 198 SCEAUX 198/", upTo(3), between(-15.0, -90.0), anyDay());
    add("LEROY MERLIN MASSY 324 54235", upTo(3), anyOf(-15.0, -90.0, -50.0, -89.0, -190.0), anyDay());
    add("CASTORAMA FRESNES ZRET2", upTo(3), anyOf(-9.5, -65.2, -34.8, -89.0, -119.0), anyDay());
    add("LDLC 34 2456F24", sometimes(), between(-39.0, -200.0), anyDay());
    add("FNAC VELIZY 234RF5", sometimes(), between(-19.0, -200.0), anyDay());
  }

  public void run(int min, int max) {
    repository.startChangeSet();
    for (Integer month : Month.range(min, max)) {
      for (MonthGenerator generator : generators) {
        generator.run(month, max);
      }
    }
    repository.completeChangeSet();
  }

  public void write(String fileName) throws IOException {
    File file = new File(fileName);

    GlobPrinter.print(repository, Account.TYPE);

    System.out.println("Output: " + file.getAbsolutePath());

    FileWriter writer = new FileWriter(file);
    OfxExporter.write(repository, writer, true);
    writer.close();
  }

  private void setAccount(String accountNumber, boolean isCardAccount, final String updateDate) {
    accountId = repository.create(Account.TYPE,
                                  value(Account.NAME, accountNumber),
                                  value(Account.BANK, BANK_ID),
                                  value(Account.BRANCH_ID, BRANCH_ID),
                                  value(Account.NUMBER, accountNumber),
                                  value(Account.CARD_TYPE, AccountCardType.DEFERRED.getId()),
                                  value(Account.POSITION_DATE, Dates.parse(updateDate)),
                                  value(Account.POSITION_WITH_PENDING, -1050.12))
      .get(Account.ID);
  }

  private void add(final String label,
                   final CountGenerator count,
                   final AmountGenerator amount,
                   final DayGenerator dayGenerator) {
    generators.add(new MonthGenerator(accountId) {
      public void run(Integer month, int maxMonth) {
        int countValue = count.get(month);
        for (int i = 0; i < countValue; i++) {
          int day = dayGenerator.get(month);
          if ((month == maxMonth) && (day > 15)) {
            continue;
          }
          create(month, day, amount.get(), label, super.accountId);
        }
      }
    });
  }

  private Glob create(Integer month, int day, double amount, String label, int accountId) {
    return repository.create(Transaction.TYPE,
                             value(Transaction.ACCOUNT, accountId),
                             value(Transaction.AMOUNT, amount),
                             value(Transaction.LABEL, label),
                             value(Transaction.ORIGINAL_LABEL, label),
                             value(Transaction.MONTH, month),
                             value(Transaction.DAY, day),
                             value(Transaction.BANK_MONTH, month),
                             value(Transaction.BANK_DAY, day),
                             value(Transaction.TRANSACTION_TYPE, TransactionType.VIREMENT.getId()));
  }

}
