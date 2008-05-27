package org.designup.picsou.importer.analyzer;

import org.crossbowlabs.globs.metamodel.GlobModel;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.GlobRepositoryBuilder;
import org.crossbowlabs.globs.xml.XmlGlobParser;
import org.designup.picsou.model.TransactionType;
import org.designup.picsou.model.TransactionTypeMatcher;
import static org.designup.picsou.model.TransactionTypeMatcher.*;

import java.io.InputStreamReader;

public class TransactionAnalyzerFactory {

  private GlobModel repository;
  private DefaultTransactionAnalyzer analyzer;

  public TransactionAnalyzerFactory(GlobModel repository) {
    this.repository = repository;
    this.analyzer = new DefaultTransactionAnalyzer();
    loadMatchers();
    analyzer.add(new LabelForCategorizationUpdater());
  }

  public TransactionAnalyzer getAnalyzer() {
    return analyzer;
  }

  private void loadMatchers() {
    GlobRepository tempGlobRepository = GlobRepositoryBuilder.init().add(TransactionType.values()).get();
    parseDefinitionFile(tempGlobRepository);
    registerMatchers(tempGlobRepository);
  }

  private void parseDefinitionFile(GlobRepository tempGlobRepository) {
    String path = "/banks/matchers.xml";
    InputStreamReader reader = new InputStreamReader(getClass().getResourceAsStream(path));
    XmlGlobParser.parse(repository, tempGlobRepository, reader, "globs");
  }

  private void registerMatchers(GlobRepository tempGlobRepository) {
    for (Glob matcher : tempGlobRepository.getAll(TransactionTypeMatcher.TYPE).sort(TransactionTypeMatcher.ID)) {
      String regexp = matcher.get(REGEXP);
      TransactionType type = TransactionType.get(matcher);
      Integer groupForLabel = matcher.get(GROUP_FOR_LABEL);
      Integer groupForDate = matcher.get(GROUP_FOR_DATE);
      if ((groupForLabel != null) && (groupForDate != null)) {
        analyzer.addExclusive(regexp, type, groupForLabel, groupForDate);
      }
      else if (groupForLabel != null) {
        analyzer.addExclusive(regexp, type, groupForLabel);
      }
      else {
        analyzer.addExclusive(regexp, type);
      }
    }
  }
}
