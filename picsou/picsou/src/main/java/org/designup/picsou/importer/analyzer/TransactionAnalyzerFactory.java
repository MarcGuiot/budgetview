package org.designup.picsou.importer.analyzer;

import org.crossbowlabs.globs.metamodel.GlobModel;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.utils.exceptions.ResourceAccessFailed;
import org.crossbowlabs.globs.xml.XmlGlobParser;
import org.designup.picsou.model.TransactionType;
import org.designup.picsou.model.TransactionTypeMatcher;
import static org.designup.picsou.model.TransactionTypeMatcher.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TransactionAnalyzerFactory {

  private GlobModel modelRepository;
  private GlobRepository globRepository;
  private DefaultTransactionAnalyzer analyzer;

  public TransactionAnalyzerFactory(GlobModel modelRepository, GlobRepository globRepository) {
    this.modelRepository = modelRepository;
    this.globRepository = globRepository;
    this.analyzer = new DefaultTransactionAnalyzer();
    loadMatchers();
    analyzer.add(new LabelForCategorizationUpdater());
  }

  public TransactionAnalyzer getAnalyzer() {
    return analyzer;
  }

  private void loadMatchers() {
    parseDefinitionFile(globRepository);
    registerMatchers(globRepository);
  }

  private void parseDefinitionFile(GlobRepository globRepository) {
    String bankListFileName = "/banks/bankList.txt";
    InputStream bankListStream = getClass().getResourceAsStream(bankListFileName);
    if (bankListStream == null) {
      throw new ResourceAccessFailed("missing bank file list'" + bankListFileName + "'");
    }
    BufferedReader bankListReader =
      new BufferedReader(new InputStreamReader(bankListStream));
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
      String path = "/banks/" + bankFileName;
      InputStream stream = getClass().getResourceAsStream(path);
      if (stream != null) {
        InputStreamReader reader = new InputStreamReader(stream);
        XmlGlobParser.parse(modelRepository, globRepository, reader, "globs");
      }
      else {
        throw new ResourceAccessFailed("missing bank file '" + path + "'");
      }
    }
  }

  private void registerMatchers(GlobRepository globRepository) {
    for (Glob matcher : globRepository.getAll(TransactionTypeMatcher.TYPE).sort(TransactionTypeMatcher.ID)) {
      String regexp = matcher.get(REGEXP);
      TransactionType type = TransactionType.get(matcher);
      Integer groupForLabel = matcher.get(GROUP_FOR_LABEL);
      Integer groupForDate = matcher.get(GROUP_FOR_DATE);
      Glob bank = globRepository.findLinkTarget(matcher, BANK);
      if ((groupForLabel != null) && (groupForDate != null)) {
        analyzer.addExclusive(regexp, type, groupForLabel, groupForDate, bank);
      }
      else if (groupForLabel != null) {
        analyzer.addExclusive(regexp, type, groupForLabel, bank);
      }
      else {
        analyzer.addExclusive(regexp, type, bank);
      }
    }
  }
}
