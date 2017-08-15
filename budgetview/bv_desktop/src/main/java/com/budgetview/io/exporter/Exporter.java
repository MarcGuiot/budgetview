package com.budgetview.io.exporter;

import org.globsframework.model.GlobRepository;
import org.globsframework.utils.exceptions.ResourceAccessFailed;

import java.io.IOException;
import java.io.Writer;

public interface Exporter {

  String getType();

  String getExtension();

  void export(GlobRepository repository, Writer writer) throws ResourceAccessFailed, IOException;
}
