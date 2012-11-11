package com.budgetview.android;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.budgetview.shared.model.AccountEntity;
import com.budgetview.shared.model.SeriesEntity;
import com.budgetview.shared.model.TransactionValues;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.exceptions.InvalidParameter;

public class TransactionPageFragment extends Fragment {

  public static String TRANSACTION_ID_PARAMETER = "transactionPageFragment.parameters.transactionId";

  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    Bundle args = getArguments();
    if (!args.containsKey(TRANSACTION_ID_PARAMETER)) {
      throw new InvalidParameter("Missing argument " + TRANSACTION_ID_PARAMETER);
    }

    View view = inflater.inflate(R.layout.transaction_page, container, false);

    GlobRepository repository = ((App)getActivity().getApplication()).getRepository();

    int transactionId = args.getInt(TRANSACTION_ID_PARAMETER, -1);
    Glob transaction = repository.get(Key.create(TransactionValues.TYPE, transactionId));

    Views.setText(view, R.id.transaction_label, transaction.get(TransactionValues.LABEL));
    Views.setColoredText(view, R.id.transaction_amount, transaction.get(TransactionValues.AMOUNT));
    Views.setDate(view, R.id.transaction_date,
                  transaction.get(TransactionValues.BANK_MONTH),
                  transaction.get(TransactionValues.BANK_DAY),
                  getResources());

    Glob account = repository.findLinkTarget(transaction, TransactionValues.ACCOUNT);
    Views.setText(view, R.id.transaction_account, account.get(AccountEntity.LABEL));

    Glob series = repository.findLinkTarget(transaction, TransactionValues.SERIES);
    if (series != null) {
      Views.setText(view, R.id.transaction_series, series.get(SeriesEntity.NAME));
    }

    return view;
  }

}
