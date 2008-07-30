package org.designup.picsou.functests.utils;

import org.designup.picsou.client.ServerAccess;
import org.designup.picsou.model.MasterCategory;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.delta.MutableChangeSet;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class DummyServerAccess implements ServerAccess {

  private SortedMap<String, Integer> keywordsToCategories = new TreeMap<String, Integer>();
  private Map<String, Integer> learnedAllocations = new HashMap<String, Integer>();
  private int nextId = 1000;

  public DummyServerAccess() {
    registerKeywords();
  }

  public void applyChanges(ChangeSet changeSet, GlobRepository globRepository) {
  }

  public void takeSnapshot() {
  }

  public void connect() {
  }

  public void addTransaction(GlobList transactions) {
  }

  public GlobList getUserData(MutableChangeSet changeSet, IdUpdate idUpdate) {
    return GlobList.EMPTY;
  }

  public boolean createUser(String name, char[] password) {
    return false;
  }

  public boolean initConnection(String name, char[] password, boolean privateComputer) {
    return false;
  }

  public void register(byte[] mail, byte[] signature, String activationCode) {
  }

  public void disconnect() {
  }

  private Integer getCategoryId(String label) {
    Integer learnedId = learnedAllocations.get(label);
    if (learnedId != null) {
      return learnedId;
    }
    String lowerCaseLabel = label.toLowerCase();
    for (Map.Entry<String, Integer> entry : keywordsToCategories.entrySet()) {
      if (lowerCaseLabel.contains(entry.getKey())) {
        return entry.getValue();
      }
    }
    return null;
  }

  private void registerKeywords() {
    add(MasterCategory.TAXES,
        "tresor public",
        "somewhere");
    add(MasterCategory.CLOTHING,
        "vert baudet",
        "somewhere",
        "dpam",
        "something else",
        "autour de bebe");
    add(MasterCategory.BANK,
        "bnp",
        "cic");
    add(MasterCategory.HEALTH,
        "pharm",
        "agf",
        "r.a.m",
        "hopital",
        "ram pl");
    add(MasterCategory.TRANSPORTS,
        "station",
        "elf",
        "total",
        "vincipark",
        "sarl silvares",
        "sapn",
        "bp",
        "sncf",
        "ratp");
    add(MasterCategory.EDUCATION,
        "scolarite");
    add(MasterCategory.HOUSE,
        "edf",
        "gdf",
        "truffaut",
        "gaz de france");
    add(MasterCategory.FOOD,
        "auchan",
        "monoprix",
        "kalistea",
        "bistrot",
        "atac",
        "nespresso",
        "mc donald's",
        "global nature",
        "marc savier",
        "bio",
        "delclere");
    add(MasterCategory.TELECOMS,
        "telecom",
        "orange france",
        "internet",
        "sfr");
    add(MasterCategory.MULTIMEDIA,
        "grosbill");
    add(MasterCategory.LEISURES,
        "go sport",
        "decathlon",
        "alapage",
        "ugc",
        "sport",
        "eveil - jeux",
        "toys",
        "nature et decou");
  }

  private void add(MasterCategory category, String... samples) {
    for (String sample : samples) {
      add(sample, category);
    }
  }

  private void add(String keyword, MasterCategory category) {
    keywordsToCategories.put(keyword, category.getId());
  }
}

