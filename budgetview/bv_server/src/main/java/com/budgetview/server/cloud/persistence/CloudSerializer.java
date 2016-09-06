package com.budgetview.server.cloud.persistence;

import com.budgetview.server.cloud.model.ProviderAccount;
import com.budgetview.server.cloud.model.ProviderTransaction;
import com.budgetview.server.config.ConfigService;
import com.budgetview.shared.encryption.PasswordEncryption;
import com.budgetview.shared.utils.GlobSerializer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobBuilder;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidParameter;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class CloudSerializer {

  private static final String SALT = "3#D! DF?";

  private Serializer accountReader = new Serializer(ProviderAccount.TYPE, ProviderAccount.ID, new ProviderAccount.Serializer());
  private Serializer transactionReader = new Serializer(ProviderTransaction.TYPE, ProviderTransaction.ID, new ProviderTransaction.Serializer());
  private PasswordEncryption passwordEncryption;

  public CloudSerializer(Directory directory) throws Exception {
    ConfigService configService = directory.get(ConfigService.class);
    String encryptionPassword = configService.get("budgetview.db.encryption.password");
    passwordEncryption = new PasswordEncryption(encryptionPassword.toCharArray(), SALT);
  }

  public byte[] toBlob(GlobRepository repository) throws IOException, GeneralSecurityException {

    ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
    SerializedOutput output = SerializedInputOutputFactory.init(byteOutput);

    GlobList accounts = repository.getAll(ProviderAccount.TYPE);
    GlobList transactions = repository.getAll(ProviderTransaction.TYPE);

    output.writeInteger(accounts.size() + transactions.size());
    accountReader.serializeAll(accounts, repository, output);
    transactionReader.serializeAll(transactions, repository, output);

    byteOutput.close();
    return passwordEncryption.encodeData(byteOutput.toByteArray());
  }


  public void readBlob(byte[] blob, GlobRepository repository) throws IOException, GeneralSecurityException {

    byte[] decoded = passwordEncryption.decodeData(blob);
    SerializedInput input = SerializedInputOutputFactory.init(new ByteArrayInputStream(decoded));
    Integer count = input.readInteger();

    for (int i = 0; i < count; i++) {
      String typeName = input.readUtf8String();
      getReader(typeName).deserializeGlob(input, repository);
    }
  }

  private Serializer getReader(String typeName) {
    if (typeName.equalsIgnoreCase(ProviderAccount.TYPE.getName())) {
      return accountReader;
    }
    else if (typeName.equalsIgnoreCase(ProviderTransaction.TYPE.getName())) {
      return transactionReader;
    }
    throw new InvalidParameter("Unexpected type:" + typeName);
  }

  private class Serializer {
    private GlobType type;
    private IntegerField idField;
    private GlobSerializer serializer;

    public Serializer(GlobType type, IntegerField idField, GlobSerializer serializer) {
      this.type = type;
      this.idField = idField;
      this.serializer = serializer;
    }

    public void serializeAll(GlobList globs, GlobRepository repository, SerializedOutput output) {
      int version = serializer.getWriteVersion();
      for (Glob glob : globs) {
        if (serializer.shouldBeSaved(repository, glob)) {
          output.writeUtf8String(type.getName());
          output.writeInteger(version);
          output.writeInteger(glob.get(idField));
          output.writeBytes(serializer.serializeData(glob));
        }
      }
    }

    public void deserializeGlob(SerializedInput input, GlobRepository repository) {
      int version = input.readInteger();
      int id = input.readInteger();
      GlobBuilder builder = GlobBuilder.init(type);
      serializer.deserializeData(version, input.readBytes(), id, builder);
      builder.set(idField, id);
      Glob glob = builder.get();
      repository.create(type, glob.toArray());
    }
  }
}
