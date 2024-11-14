package com.example.messenger.Authentication;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.messenger.Authentication.Class.CountryCodeHelper;
import com.example.messenger.Authentication.Class.CountryList;
import com.example.messenger.CustomSpinnerAdapter;
import com.example.messenger.Model.Users;
import com.example.messenger.PhoneTextWatcher;
import com.example.messenger.Profile;
import com.example.messenger.R;
import com.example.messenger.Reotrfit.Api;
import com.example.messenger.Reotrfit.RetrofitService;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Authorization extends AppCompatActivity {

    private Spinner spinner;
    private EditText et_number, et_phone;
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private Dialog dialog;
    private EditText number1, number2, number3, number4, number5, number6;
    private Button btn_registration;
    private ImageButton btn_back;
    private FirebaseUser user;
    private RetrofitService retrofitService;
    private  String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_authorization);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        spinner = findViewById(R.id.spinner);
        et_number = findViewById(R.id.et_number);
        et_phone = findViewById(R.id.et_phone);
        new PhoneTextWatcher(et_phone);
        mAuth = FirebaseAuth.getInstance();
        // Инициализация RetrofitService
        retrofitService = new RetrofitService();
        btn_back = findViewById(R.id.btn_back);
        btn_back.setOnClickListener(v->{
            Intent intent = new Intent(Authorization.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0); // Убрать анимацию перехода
        });

        List<String> countries = CountryList.getCountries();

        CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(this, android.R.layout.simple_spinner_item, countries);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedCountry = countries.get(position);
                String countryCode = CountryCodeHelper.getCountryCode(selectedCountry);
                et_number.setText("+" + countryCode);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        Button btn_autho = findViewById(R.id.btn_autho);
        btn_autho.setOnClickListener(v -> {
            phoneNumber = "+" + (et_number.getText().toString() + et_phone.getText().toString().trim()).replaceAll("[^0-9]", "");
            if (spinner.getSelectedItemPosition() == 0) {
                Toast.makeText(Authorization.this, "Выберите страну", Toast.LENGTH_SHORT).show();
            } else if (phoneNumber != null && phoneNumber.length() == 12) {
                // Запускаем асинхронную проверку номера телефона
                checkPhoneNumber(phoneNumber);
            } else {
                Toast.makeText(Authorization.this, "Ошибка! Проверьте введенные данные", Toast.LENGTH_SHORT).show();
            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                // Обработка ошибок верификации номера телефона
                Toast.makeText(Authorization.this, "Ошибка верификации номера телефона: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Некорректный формат номера телефона
                    Toast.makeText(Authorization.this, "Некорректный формат номера телефона", Toast.LENGTH_SHORT).show();
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // Превышение лимита запросов на верификацию
                    Toast.makeText(Authorization.this, "Превышен лимит запросов на верификацию. Попробуйте позже", Toast.LENGTH_SHORT).show();
                } else {
                    // Другие типы ошибок
                    Toast.makeText(Authorization.this, "Произошла ошибка верификации номера телефона", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCodeSent(@NonNull String verification, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                dialog.show();

                number1.requestFocus();
                setEditTextAutoAdvance(number1, number2);
                setEditTextAutoAdvance(number2, number3);
                setEditTextAutoAdvance(number3, number4);
                setEditTextAutoAdvance(number4, number5);
                setEditTextAutoAdvance(number5, number6);

                Toast.makeText(Authorization.this, "Код верификации отправлен", Toast.LENGTH_SHORT).show();
                btn_registration.setOnClickListener(v -> {
                    String inputCode = number1.getText().toString() + number2.getText().toString() + number3.getText().toString() +
                            number4.getText().toString() + number5.getText().toString() + number6.getText().toString();

                    if (TextUtils.isEmpty(inputCode)) {
                        Toast.makeText(Authorization.this, "Введите полученный код", Toast.LENGTH_SHORT).show();
                    } else {
                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verification, inputCode);
                        signInWithPhoneAuthCredential(credential);

                        // Скрытие клавиатуры
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                });
            }
        };
    }
    //Метод проверки номера телефона на наличие в бд
    private void checkPhoneNumber(String phoneNumber) {
        Api apiService = retrofitService.getRetrofit().create(Api.class);
        Call<Boolean> call = apiService.getPhoneNumber(phoneNumber);
        call.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful()) {
                    boolean exists = response.body(); // true или false
                    if (exists) {
                        // Номер телефона уже существует
                        showCodeInputDialog();
                    } else {
                        // Если номер телефона не существует, открываем диалоговое окно для ввода кода
                        new AlertDialog.Builder(Authorization.this)
                                .setTitle("Профиль не существует")
                                .setMessage("Профиль с этим номером не найден. Вы можете перейти на страницу регистрации или изменить номер.")
                                .setPositiveButton("Перейти на регистрацию", (dialog, which) -> {
                                    Intent intent = new Intent(Authorization.this, Registration.class);
                                    startActivity(intent);
                                    overridePendingTransition(0, 0);
                                })
                                .setNegativeButton("Изменить номер", (dialog, which) -> {
                                    dialog.dismiss(); // Закрываем диалог
                                    et_phone.requestFocus(); // Устанавливаем фокус на EditText для ввода номера
                                })
                                .show();
                    }
                } else {
                    Toast.makeText(Authorization.this, "Ошибка при проверке номера телефона", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Log.e("Ответ от сервера", "Произошла ошибка при проверке номера телефона " + t.getMessage());
                Toast.makeText(Authorization.this, "Ошибка связи. Попробуйте еще раз.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Открытие диалогового окна для ввода кода
    private void showCodeInputDialog() {
        // Если телефон имеет верный формат, то открываем диалоговое окно для ввода кода
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.activity_code);

        number1 = dialog.findViewById(R.id.number1);
        number2 = dialog.findViewById(R.id.number2);
        number3 = dialog.findViewById(R.id.number3);
        number4 = dialog.findViewById(R.id.number4);
        number5 = dialog.findViewById(R.id.number5);
        number6 = dialog.findViewById(R.id.number6);
        btn_registration = dialog.findViewById(R.id.btn_registration);

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                new PhoneAuthOptions.Builder(FirebaseAuth.getInstance())
                        .setPhoneNumber(phoneNumber)       // Укажите номер телефона
                        .setTimeout(60L, TimeUnit.SECONDS) // Укажите таймаут
                        .setActivity(this)                 // Укажите контекст
                        .setCallbacks(mCallbacks)          // Укажите коллбэки
                        .build()
        );

        // Показываем диалог
        dialog.show();
    }

    //Метод для переключения фокуса при вводе кода
    private void setEditTextAutoAdvance(final EditText currentEditText, final EditText nextEditText) {
        currentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1) {
                    nextEditText.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    //Метод проверки аутентификации
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        user = task.getResult().getUser();
                        if (user != null) {
                            Toast.makeText(Authorization.this, "Аутентификация успешна", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            if(user.getUid() != null){
                                getProfileUser(user.getUid());
                            }
                        } else {
                            Toast.makeText(this, "Не удалось получить информацию о текущем пользователе", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Authorization.this, Registration.class);
                            startActivity(intent);
                            overridePendingTransition(0, 0); // Убрать анимацию перехода
                        }
                    } else {
                        Toast.makeText(this, "Ошибка аутентификации с помощью SMS", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //Метод, который проверяет данные пользователя в БД
    private void getProfileUser(String userId){
        Api apiService = retrofitService.getRetrofit().create(Api.class);
        // Выполняем асинхронный запрос
        Call<Users> call = apiService.getProfileUser(userId);

        call.enqueue(new Callback<Users>() {
            @Override
            public void onResponse(Call<Users> call, Response<Users> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Intent intent = new Intent(Authorization.this, Profile.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0); // Убрать анимацию перехода
                } else {
                    // Обработка случая, когда пользователь не найден
                    Log.e("API", "Пользователь не найден или ошибка в ответе: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Users> call, Throwable t) {
                // Обработка ошибки, например, ошибка сети
                Log.e("API", "Ошибка при получении данных профиля: " + t.getMessage());
            }
        });

    }
}