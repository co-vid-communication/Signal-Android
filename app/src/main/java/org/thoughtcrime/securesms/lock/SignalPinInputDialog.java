package org.thoughtcrime.securesms.lock;

import android.content.Context;
import android.os.Build;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.DialogCompat;

import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.keyvalue.SignalStore;
import org.thoughtcrime.securesms.util.ThemeUtil;

import java.util.Objects;

public final class SignalPinInputDialog {


    public static void show(@NonNull Context context, @NonNull Callback callback) {
        if (!SignalStore.kbsValues().hasPin() || SignalStore.kbsValues().hasOptedOut()) {
            callback.onPinCorrect();  // we have no PIN to check here
        }

        AlertDialog dialog = new AlertDialog.Builder(context, ThemeUtil.isDarkTheme(context) ? R.style.Theme_Signal_AlertDialog_Dark_Cornered_ColoredAccent : R.style.Theme_Signal_AlertDialog_Light_Cornered_ColoredAccent)
                .setCancelable(false)
                .setView(R.layout.pin_input_dialog)
                .setTitle(context.getString(R.string.KbsReminderDialog__enter_your_signal_pin))
                .create();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dialog.create();
        } else {
            dialog.show();
        }

        TextView pinStatus = (TextView) DialogCompat.requireViewById(dialog, R.id.pin_input_status);
        EditText pinEditText = (EditText) DialogCompat.requireViewById(dialog, R.id.pin_input_pin);
        Button check = (Button) DialogCompat.requireViewById(dialog, R.id.pin_input_check);
        Button cancel = (Button) DialogCompat.requireViewById(dialog, R.id.pin_input_cancel);

        switch (SignalStore.pinValues().getKeyboardType()) {
            case NUMERIC:
                pinEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
                break;
            case ALPHA_NUMERIC:
                pinEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                break;
        }

        check.setOnClickListener(view -> {
            String pinInput = pinEditText.getText().toString();
            if(pinInput.isEmpty()) {
                pinStatus.setText(context.getString(R.string.pin_input_noinput));
                pinStatus.setVisibility(View.VISIBLE);
            } else {
                final String localHash = Objects.requireNonNull(SignalStore.kbsValues().getLocalPinHash());
                if(PinHashing.verifyLocalPinHash(localHash, pinInput)) {
                    callback.onPinCorrect();
                    dialog.dismiss();
                } else {
                    pinStatus.setText(context.getString(R.string.pin_input_wrongpin));
                    pinStatus.setVisibility(View.VISIBLE);
                }
            }
        });

        pinEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                pinStatus.setText("");
                pinStatus.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

        cancel.setOnClickListener(view -> {
            callback.onDismissed();
            dialog.dismiss();
        });

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dialog.show();
        }
    }


    public interface Callback {
        void onPinCorrect();
        void onDismissed();
    }
}
