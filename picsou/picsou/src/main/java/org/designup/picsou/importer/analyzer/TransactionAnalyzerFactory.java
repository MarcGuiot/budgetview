package org.designup.picsou.importer.analyzer;

import org.designup.picsou.model.PreTransactionTypeMatcher;
import org.designup.picsou.model.TransactionType;
import org.designup.picsou.model.TransactionTypeMatcher;
import static org.designup.picsou.model.TransactionTypeMatcher.*;
import org.globsframework.metamodel.GlobModel;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Strings;
import org.globsframework.utils.exceptions.InvalidFormat;
import org.globsframework.utils.exceptions.ResourceAccessFailed;
import org.globsframework.xml.XmlGlobParser;

import java.io.*;

public class TransactionAnalyzerFactory {

  private GlobModel modelRepository;
  private GlobRepository globRepository;
  private DefaultTransactionAnalyzer analyzer;
  private Long version = 0L;

  public TransactionAnalyzerFactory(GlobModel modelRepository, GlobRepository globRepository) {
    this.modelRepository = modelRepository;
    this.globRepository = globRepository;
  }

  public interface Loader {
    InputStream load(String file);
  }

  synchronized public void load(final ClassLoader loader, Long version) {
    load(new Loader() {
      public InputStream load(String file) {
        return loader.getResourceAsStream(file);
      }
    }, version);
  }

  synchronized public void load(Loader loader, Long version) {
    if (this.version < version) {
      this.version = version;
      this.analyzer = new DefaultTransactionAnalyzer();
      loadMatchers(loader);
      analyzer.add(new LabelForCategorizationUpdater());
      analyzer.add(new TransactionDateUpdater());
    }
  }

  synchronized public TransactionAnalyzer getAnalyzer() {
    return analyzer;
  }

  private void loadMatchers(Loader loader) {
    parseDefinitionFile(globRepository, loader);
    registerMatchers(globRepository);
  }

  private void parseDefinitionFile(GlobRepository globRepository, Loader loader) {
    String bankListFileName = "banks/bankList.txt";
    InputStream bankListStream = loader.load(bankListFileName);

    if (bankListStream == null) {
      throw new ResourceAccessFailed("missing bank file list'" + bankListFileName + "'");
    }
    BufferedReader bankListReader;
    try {
      bankListReader = new BufferedReader(new InputStreamReader(bankListStream, "UTF-8"));
    }
    catch (UnsupportedEncodingException e) {
      throw new InvalidFormat(e);
    }
    while (true) {
      String bankFileName;
      try {
        bankFileName = bankListReader.readLine();
      }
      catch (IOException e) {
        throw new ResourceAccessFailed(e);
      }
      if (bankFileName == null) {
        break;
      }
      String path = "banks/" + bankFileName;
      InputStream stream = loader.load(path);
      if (stream != null) {
        InputStreamReader reader = null;
        try {
          reader = new InputStreamReader(stream, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
          throw new ResourceAccessFailed(e);
        }
        XmlGlobParser.parse(modelRepository, globRepository, reader, "globs");
      }
      else {
        throw new ResourceAccessFailed("missing bank file '" + path + "'");
      }
    }
  }

  private void registerMatchers(GlobRepository globRepository) {
//    for (Glob matcher : globRepository.getAll(TransactionTypeMatcher.TYPE).sort(TransactionTypeMatcher.ID)) {
//      String regexp = matcher.get(REGEXP);
//      String regexpForType = matcher.get(BANK_TYPE);
//      TransactionType type = TransactionType.get(matcher);
//      String labelRegexp = matcher.get(LABEL);
//      Integer groupForDate = matcher.get(GROUP_FOR_DATE);
//      String dateFormat = matcher.get(DATE_FORMAT);
//      if ((groupForDate != null) && (Strings.isNullOrEmpty(dateFormat))) {
//        throw new RuntimeException("You must specify a date format");
//      }
//
//      Glob bank = globRepository.findLinkTarget(matcher, BANK);
//      if ((labelRegexp != null) && (groupForDate != null)) {
//        analyzer.addExclusive(regexp, type, regexpForType, labelRegexp, groupForDate, dateFormat, bank);
//      }
//      else if (labelRegexp != null) {
//        analyzer.addExclusive(regexp, type, regexpForType, labelRegexp, bank);
//      }
//      else {
//        analyzer.addExclusive(regexp, type, regexpForType, bank);
//      }
//    }
    for (Glob matcher : globRepository.getAll(PreTransactionTypeMatcher.TYPE)
      .sort(PreTransactionTypeMatcher.ID)) {
      String label = matcher.get(PreTransactionTypeMatcher.LABEL);
      String originalLabel = matcher.get(PreTransactionTypeMatcher.ORIGINAL_LABEL);
      Glob bank = globRepository.findLinkTarget(matcher, PreTransactionTypeMatcher.BANK);
      TransactionType type = TransactionType.get(matcher);
      String bankType = matcher.get(PreTransactionTypeMatcher.BANK_TYPE);
      String groupForDate = matcher.get(PreTransactionTypeMatcher.GROUP_FOR_DATE);
      String dateFormat = matcher.get(PreTransactionTypeMatcher.DATE_FORMAT);

      String name = matcher.get(PreTransactionTypeMatcher.OFX_NAME);
      String memo = matcher.get(PreTransactionTypeMatcher.OFX_MEMO);
      String checkNum = matcher.get(PreTransactionTypeMatcher.OFX_CHECK_NUM);
      if (name != null || memo != null || checkNum != null) {
        if (label != null) {
          analyzer.addOfx(name, memo, checkNum, label, bank, bankType, groupForDate, dateFormat, type);
          groupForDate = null; //pour ne pas updater deux fois la date
          dateFormat = null;
        }
        if (originalLabel != null) {
          analyzer.addOriginalOfx(name, memo, checkNum, originalLabel, bank, bankType,
                                  groupForDate, dateFormat, type);
        }
      }
      else {
        String qifM = matcher.get(PreTransactionTypeMatcher.QIF_M);
        String qifP = matcher.get(PreTransactionTypeMatcher.QIF_P);
        if (label != null) {
          analyzer.addQif(qifM, qifP, label, bank, bankType, groupForDate, dateFormat, type);
          groupForDate = null; //pour ne pas updater deux fois la date
          dateFormat = null;
        }
        if (originalLabel != null) {
          analyzer.addOriginalQif(qifM, qifP, originalLabel, bank, bankType,
                                  groupForDate, dateFormat, type);
        }
      }
    }
  }
}
