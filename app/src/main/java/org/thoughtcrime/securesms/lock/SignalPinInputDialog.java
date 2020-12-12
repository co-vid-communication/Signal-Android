package org.thoughtcrime.securesms.lock;

import android.content.Context;
import android.os.Build;
import android.text.InputType;
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
        if (!SignalStore.kbsValues().hasPin()) {
            throw new AssertionError("Must have a PIN!");
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
                Toast.makeText(context, context.getString(R.string.pin_input_noinput), Toast.LENGTH_LONG).show();
            } else {
                final String localHash = Objects.requireNonNull(SignalStore.kbsValues().getLocalPinHash());
                if(PinHashing.verifyLocalPinHash(localHash, pinInput)) {
                    callback.onPinCorrect();
                } else {
                    callback.onPinWrong();
                }
            }
            dialog.dismiss();
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
        void onPinWrong();
        void onDismissed();
    }
}
