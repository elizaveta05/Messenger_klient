package com.example.messenger;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class PhoneTextWatcher implements TextWatcher {

    private EditText editText;
    private String current = "";
    private String phoneFormat = "### ### - ## - ##";

    public PhoneTextWatcher(EditText editText) {
        this.editText = editText;
        editText.addTextChangedListener(this);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {
        if (!s.toString().equals(current)) {
            String clean = s.toString().replaceAll("[^\\d]", "");
            String formatted = "";
            int len = clean.length();
            int index = 0;
            for (int i = 0; i < phoneFormat.length(); i++) {
                if (index >= len) {
                    break;
                }
                if (phoneFormat.charAt(i) == '#') {
                    formatted += clean.charAt(index);
                    index++;
                } else {
                    formatted += phoneFormat.charAt(i);
                }
            }
            current = formatted;
            editText.setText(formatted);
            editText.setSelection(formatted.length());
        }
    }
}