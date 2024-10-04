package com.example.messenger.authentication;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.messenger.Chats;
import com.example.messenger.CustomSpinnerAdapter;
import com.example.messenger.PhoneTextWatcher;
import com.example.messenger.R;
import com.example.messenger.Users;
import com.example.messenger.reotrfit.Api;
import com.example.messenger.reotrfit.RetrofitService;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Registration extends AppCompatActivity {

    private ImageView photoImageView;
    private ImageButton addPhotoButton;
    private Bitmap selectedImageBitmap;
    private Spinner spinner;
    private Button btn_autho;
    private EditText et_number, et_phone;
    private String enteredLogin;
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private Dialog dialog;
    private EditText number1, number2, number3, number4, number5, number6;
    private Button btn_registration;
    private ImageButton btn_back;
    private  String phoneNumber;
    private FirebaseUser user;
    private List<String> allLogins;
    private RetrofitService retrofitService;

    ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri result) {
                    try {
                        selectedImageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), result);
                        photoImageView.setImageBitmap(selectedImageBitmap);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
                    }
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        spinner = findViewById(R.id.spinner);
        photoImageView = findViewById(R.id.image_photo_user);
        addPhotoButton = findViewById(R.id.btn_add_photo);
        et_number = findViewById(R.id.et_number);
        et_phone = findViewById(R.id.et_phone);
        btn_autho = findViewById(R.id.btn_autho);
        new PhoneTextWatcher(et_phone);
        mAuth = FirebaseAuth.getInstance();
        // Инициализируем RetrofitService
        retrofitService = new RetrofitService();
        btn_back = findViewById(R.id.btn_back);
        btn_back.setOnClickListener(v->{
            Intent intent = new Intent(Registration.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0); // Убрать анимацию перехода
        });

        TextInputLayout textInputLayoutLogin = findViewById(R.id.textInputLayoutLogin);
        EditText et_login = textInputLayoutLogin.getEditText();

        if (et_login != null) {

            et_login.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    enteredLogin = s.toString();
                    boolean isValid = isValidLogin(enteredLogin);

                    if (isValid) {
                        textInputLayoutLogin.setBoxStrokeColor(getResources().getColor(R.color.green));
                    } else {
                        textInputLayoutLogin.setBoxStrokeColor(getResources().getColor(R.color.red));
                    }
                }
            });
        }
        ImageButton btnMessage = findViewById(R.id.btn_messege);
        btnMessage.setOnClickListener(v -> {
            final Toast toast = Toast.makeText(getApplicationContext(), "Логин должен содержать символы a-z, 0-9, подчеркивание и не содержать пробелы." +
                    "Минимальная длина 5 символов.", Toast.LENGTH_LONG);
            toast.show();
            new CountDownTimer(7000, 1000) {
                public void onTick(long millisUntilFinished) {
                    toast.show();
                }
                public void onFinish() {
                    toast.cancel();
                }
            }.start();
        });
        addPhotoButton.setOnClickListener(v -> {
            requestPermissionLauncher.launch("image/*");
        });
        List<String> countries = new ArrayList<>();
        countries.add("Выберите страну");
        countries.add("Россия");
        countries.add("США");
        countries.add("Китай");
        countries.add("Бразилия");
        countries.add("Германия");
        countries.add("Индия");
        countries.add("Австралия");

        CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(this, android.R.layout.simple_spinner_item, countries);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedCountry = countries.get(position);
                String countryCode = getCountryCode(selectedCountry);
                et_number.setText("+" + countryCode);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
        btn_autho.setOnClickListener(v -> {
            phoneNumber = "+" + (et_number.getText().toString() + et_phone.getText().toString().trim()).replaceAll("[^0-9]", "");
            if (spinner.getSelectedItemPosition() == 0) {
                Toast.makeText(Registration.this, "Выберите страну", Toast.LENGTH_SHORT).show();
            } else if (phoneNumber != null && phoneNumber.length() == 12 && isValidLogin(enteredLogin)) {
                //Если телефон имеет верный формат, то открываем диалоговое окно для введение кода
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
                        phoneNumber,
                        60L,
                        TimeUnit.SECONDS,
                        this,
                        mCallbacks
                );
            } else {
                Toast.makeText(Registration.this, "Ошибка! Проверьте введенные данные", Toast.LENGTH_SHORT).show();
            }
        });
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                // Обработка ошибок верификации номера телефона
                Toast.makeText(Registration.this, "Ошибка верификации номера телефона: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Некорректный формат номера телефона
                    Toast.makeText(Registration.this, "Некорректный формат номера телефона", Toast.LENGTH_SHORT).show();
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // Превышение лимита запросов на верификацию
                    Toast.makeText(Registration.this, "Превышен лимит запросов на верификацию. Попробуйте позже", Toast.LENGTH_SHORT).show();
                } else {
                    // Другие типы ошибок
                    Toast.makeText(Registration.this, "Произошла ошибка верификации номера телефона", Toast.LENGTH_SHORT).show();
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

                Toast.makeText(Registration.this, "Код верификации отправлен", Toast.LENGTH_SHORT).show();
                btn_registration.setOnClickListener(v -> {
                    String inputCode = number1.getText().toString() + number2.getText().toString() + number3.getText().toString() +
                            number4.getText().toString() + number5.getText().toString() + number6.getText().toString();

                    if (TextUtils.isEmpty(inputCode))
                        Toast.makeText(Registration.this, "Введите полученный код", Toast.LENGTH_SHORT).show();
                    else {
                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verification, inputCode);
                        signInWithPhoneAuthCredential(credential);
                    }
                });
            }
        };
    }
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
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        user = mAuth.getCurrentUser();
                        if (user != null) {
                            Toast.makeText(Registration.this, "Аутентификация успешна", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();

                            // Если изображение выбрано, загружаем его на сервер
                            if (selectedImageBitmap != null) {
                                uploadImageToFirebase(user.getUid(), new OnImageUploadCallback() {
                                    @Override
                                    public void onUploadSuccess(String imageUrl) {
                                        // Вызываем метод регистрации пользователя с imageUrl
                                        registerUser(user.getUid(), enteredLogin, phoneNumber, imageUrl);
                                    }

                                    @Override
                                    public void onUploadFailure(String errorMessage) {
                                        Toast.makeText(Registration.this, "Ошибка загрузки изображения: " + errorMessage, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                // Если изображение не выбрано, отправляем null
                                registerUser(user.getUid(), enteredLogin, phoneNumber, null);
                            }

                        } else {
                            Toast.makeText(this, "Не удалось получить информацию о текущем пользователе", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Ошибка аутентификации с помощью SMS", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Метод для регистрации пользователя
    private void registerUser(String userId, String login, String phoneNumber, String imageUrl) {
        // Создаем объект пользователя
        Users newUser = new Users(userId, login, phoneNumber, imageUrl);

        // Получаем Api сервис
        Api apiService = retrofitService.getRetrofit().create(Api.class);

        // Вызываем метод для регистрации пользователя на сервере
        Call<Users> call = apiService.registerUser(newUser);

        call.enqueue(new Callback<Users>() {
            @Override
            public void onResponse(Call<Users> call, Response<Users> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(Registration.this, "Пользователь успешно зарегистрирован", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Registration.this, "Ошибка регистрации", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Users> call, Throwable t) {
                Toast.makeText(Registration.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void uploadImageToFirebase(String userId, OnImageUploadCallback callback) {
        // Ссылка на Firebase Storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // Создаем ссылку для хранения изображения
        StorageReference userImageRef = storageRef.child("profile_images/" + userId + ".jpg");

        // Конвертируем изображение в байты
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        // Загружаем изображение в Firebase Storage
        UploadTask uploadTask = userImageRef.putBytes(data);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            // Получаем ссылку на загруженное изображение
            userImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String imageUrl = uri.toString();
                callback.onUploadSuccess(imageUrl);  // Возвращаем URL
            }).addOnFailureListener(e -> {
                callback.onUploadFailure(e.getMessage());
            });
        }).addOnFailureListener(e -> {
            callback.onUploadFailure(e.getMessage());
        });
    }

    private boolean isValidLogin(String login) {
        if (TextUtils.isEmpty(login)) {
            return false;
        }

        if (login.length() < 5) {
            return false;
        }

        if (!login.matches("[a-zA-Z0-9_]+")) {
            return false;
        }

        if (login.contains(" ")) {
            return false;
        }

        if (allLogins != null && allLogins.contains(login)) {
            return false;
        }

        return true;
    }
    private String getCountryCode(String country) {
        switch (country) {
            case "Россия":
                return "7";
            case "США":
                return "1";
            case "Китай":
                return "86";
            case "Бразилия":
                return "55";
            case "Германия":
                return "49";
            case "Индия":
                return "91";
            case "Австралия":
                return "61";
            default:
                return "";
        }
    }
}