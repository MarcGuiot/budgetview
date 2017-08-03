package com.budgetview.server.cloud.services;

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
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobBuilder;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidParameter;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class CloudSerializationService {

  private static final String SALT = "3#D! DF?";

  private Serializer accountReader = new Serializer(ProviderAccount.TYPE, ProviderAccount.ID, new ProviderAccount.Serializer());
  private Serializer transactionReader = new Serializer(ProviderTransaction.TYPE, ProviderTransaction.ID, new ProviderTransaction.Serializer());
  private PasswordEncryption passwordEncryption;

  public CloudSerializationService(String encryptionPassword, Directory directory) throws Exception {
    passwordEncryption = new PasswordEncryption(encryptionPassword.toCharArray(), SALT);
  }

  public byte[] toBlob(GlobRepository repository) throws Exception {

    ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
    SerializedOutput output = SerializedInputOutputFactory.init(byteOutput);

    GlobList accounts = repository.getAll(ProviderAccount.TYPE);
    GlobList transactions = repository.getAll(ProviderTransaction.TYPE);

    output.writeInteger(accounts.size() + transactions.size());
    accountReader.serializeAll(accounts, repository, output);
    transactionReader.serializeAll(transactions, repository, output);

    byteOutput.close();
    return encode(byteOutput.toByteArray());
  }

  public void readBlob(byte[] blob, GlobRepository repository) throws Exception {

    byte[] decoded = decode(blob);
    SerializedInput input = SerializedInputOutputFactory.init(new ByteArrayInputStream(decoded));
    Integer count = input.readInteger();

    for (int i = 0; i < count; i++) {
      String typeName = input.readUtf8String();
      Glob glob = getReader(typeName).deserializeGlob(input);
      Key key = glob.getKey();
      if (repository.contains(key)) {
        repository.update(key, glob.toArray());
      }
      else {
        repository.create(glob.getType(), glob.toArray());
      }
    }
  }

  public byte[] encode(byte[] data) throws Exception {
    return passwordEncryption.encodeData(data);
  }

  public byte[] decode(byte[] data) throws Exception {
    return passwordEncryption.decodeData(data);
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

    public Glob deserializeGlob(SerializedInput input) {
      int version = input.readInteger();
      int id = input.readInteger();
      GlobBuilder builder = GlobBuilder.init(type);
      serializer.deserializeData(version, input.readBytes(), id, builder);
      builder.set(idField, id);
      return builder.get();
    }
  }
}
