package com.example.messenger.Authentication;

import android.app.AlertDialog;
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
import android.util.Log;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.messenger.CustomSpinnerAdapter;
import com.example.messenger.PhoneTextWatcher;
import com.example.messenger.Profile;
import com.example.messenger.R;
import com.example.messenger.Model.Users;
import com.example.messenger.Reotrfit.Api;
import com.example.messenger.Reotrfit.RetrofitService;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
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
    private String phoneNumber;
    private FirebaseUser user;
    private List<String> allLogins;
    private RetrofitService retrofitService;
    private StorageReference storageRef;
    private String imagePath; // Переменная для хранения пути к изображению


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

        // Инициализация Firebase Storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // Инициализируем RetrofitService
        retrofitService = new RetrofitService();
        fetchExistingUserLogins();

        btn_back = findViewById(R.id.btn_back);
        btn_back.setOnClickListener(v -> {
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
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    enteredLogin = s.toString();
                    //В зависимости от результата прохождения логином валидации устанавливается цвет рамки
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

        //Метод установки кода телефона в зависимости от выбора страны
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

        // Нажатие на кнопку регистрации
        btn_autho.setOnClickListener(v -> {
            phoneNumber = "+" + (et_number.getText().toString() + et_phone.getText().toString().trim()).replaceAll("[^0-9]", "");
            if (spinner.getSelectedItemPosition() == 0) {
                Toast.makeText(Registration.this, "Выберите страну", Toast.LENGTH_SHORT).show();
            } else if (phoneNumber != null && phoneNumber.length() == 12 && isValidLogin(enteredLogin)) {
                // Запускаем асинхронную проверку номера телефона
                checkPhoneNumber(phoneNumber);
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
                        new AlertDialog.Builder(Registration.this)
                                .setTitle("Профиль уже существует")
                                .setMessage("Профиль с этим номером уже существует. Вы можете перейти на страницу авторизации или изменить номер.")
                                .setPositiveButton("Перейти на авторизацию", (dialog, which) -> {
                                    Intent intent = new Intent(Registration.this, Authorization.class);
                                    startActivity(intent);
                                    overridePendingTransition(0, 0);
                                })
                                .setNegativeButton("Изменить номер", (dialog, which) -> {
                                    dialog.dismiss(); // Закрываем диалог
                                    et_phone.requestFocus(); // Устанавливаем фокус на EditText для ввода номера
                                })
                                .show();
                    } else {
                        // Если номер телефона не существует, открываем диалоговое окно для ввода кода
                        showCodeInputDialog();
                    }
                } else {
                    Toast.makeText(Registration.this, "Ошибка при проверке номера телефона", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Log.e("Ответ от сервера", "Произошла ошибка при проверке номера телефона " + t.getMessage());
                Toast.makeText(Registration.this, "Ошибка связи. Попробуйте еще раз.", Toast.LENGTH_SHORT).show();
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

    //Метод для выбора изображения пользователем
    ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri result) {
                    try {
                        selectedImageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), result); //Присваиваем выбранное изображение переменной

                        photoImageView.setImageBitmap(selectedImageBitmap); //Установка выбранного изображения в окно отображения
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
                    }
                }
            });

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
                        user = mAuth.getCurrentUser();
                        if (user != null) {
                            Toast.makeText(Registration.this, "Аутентификация успешна", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            if(selectedImageBitmap!=null) {
                                //Если изображения проифля выбрано, то сначала отсылаем его в хранилище
                                uploadImageToFirebase(user.getUid(), enteredLogin, phoneNumber, selectedImageBitmap);
                            }else if(selectedImageBitmap == null){
                                //Если же изображение не было выбрано, то сразу регистрируем пользователя с передачей null в параметре изображения
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

    //Метод загрузки изображений в Firebase Storage
    private void uploadImageToFirebase(String userId, String login, String phoneNumber, Bitmap selectedImageBitmap) {
        //Конвертируем изображение
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        imagePath = "users_profile_image/" + userId + ".jpg"; // Сохраняем путь к изображению
        StorageReference imageRef = storageRef.child(imagePath);
        //Сохраняем изображение в хранилище
        UploadTask uploadTask = imageRef.putBytes(data);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> { //При успешной загрузке изображения в хранилище, мы передаем url изображения на регистрацию
                String imageUrl = uri.toString();
                if (imageUrl != null){
                    Log.d("FirebaseStorage", "Изображение успешно добавлено в Firebase Storage");
                    registerUser(userId, login, phoneNumber, imageUrl);
                }
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(Registration.this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show();
        });
    }
    //Метод, который отправляет запрос на регистрацию пользователей
    private void registerUser(String userId, String login, String phoneNumber, String imageUrl) {
        Api apiService = retrofitService.getRetrofit().create(Api.class);
        RequestBody userIdBody = RequestBody.create(MediaType.parse("text/plain"), userId);
        RequestBody loginBody = RequestBody.create(MediaType.parse("text/plain"), login);
        RequestBody phoneNumberBody = RequestBody.create(MediaType.parse("text/plain"), phoneNumber);

        Call<Users> call = apiService.registerUser(userIdBody, loginBody, phoneNumberBody, imageUrl != null ?
                RequestBody.create(MediaType.parse("text/plain"), imageUrl) : null);

        call.enqueue(new Callback<Users>() {
            @Override
            public void onResponse(Call<Users> call, Response<Users> response) {
                if (response.isSuccessful()) {
                    Log.d("Ответ сервера", "Пользователь зарегистрирован");
                    Toast.makeText(Registration.this, "Пользователь успешно зарегистрирован", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Registration.this, Profile.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0); // Убрать анимацию перехода

                } else {
                    Log.d("Ответ сервера", "Пользователь не зарегистрирован! Произошла ошибка!");
                    Toast.makeText(Registration.this, "Ошибка регистрации", Toast.LENGTH_SHORT).show();
                    if (imagePath != null) {
                        deleteImageFromFirebase(imagePath); // Удаляем изображение по пути
                    }
                }
            }

            @Override
            public void onFailure(Call<Users> call, Throwable t) {
                Log.d("Ответ сервера", "Пользователь не зарегистрирован! Произошла ошибка!");
                Toast.makeText(Registration.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                if (imagePath != null) {
                    deleteImageFromFirebase(imagePath); // Удаляем изображение по пути
                }
                deleteFirebaseUser(); // Удаляем пользователя из Firebase Auth
            }
        });
    }
    //Метод, который удаляет изображение из хранилища, в случае если на сервере не происходит регистрация
    private void deleteImageFromFirebase(String imagePath) {
        StorageReference imageRef = storageRef.child(imagePath);
        imageRef.delete().addOnSuccessListener(aVoid -> {
            Log.d("FirebaseStorage", "Изображение успешно удалено из Firebase Storage");
        }).addOnFailureListener(e -> {
            Log.e("FirebaseStorage", "Ошибка при удалении изображения из Firebase Storage: " + e.getMessage());
        });
    }

    //Метод для удаления пользователя из Firebase Autho в случае если его данные не были сохранены
    private void deleteFirebaseUser() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUser.delete().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d("FirebaseAuth", "Пользователь удалён из Firebase Auth");
                } else {
                    Log.e("FirebaseAuth", "Не удалось удалить пользователя из Firebase Auth: " + task.getException());
                }
            });
        }
    }

    // Метод получения всех логинов существующих пользователей
    private void fetchExistingUserLogins() {
        Api apiService = retrofitService.getRetrofit().create(Api.class);

        Call<List<String>> call = apiService.getUsersLogin();
        call.enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Если запрос успешен и тело ответа не пустое
                    allLogins = response.body();
                    Log.d("FetchLogins", "Логины успешно получены: " + allLogins);
                } else {
                    // Если ответ не успешен
                    String errorMessage = response.errorBody() != null ? response.errorBody().toString() : "Unknown Error";
                    Log.e("FetchLogins", "Ошибка получения логинов: " + errorMessage);
                }
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                Log.e("FetchLogins", "Ошибка сети: " + t.getMessage());
            }
        });
    }

    //Валидация логина
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

    //Метод получения кода страны для номер телефона
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