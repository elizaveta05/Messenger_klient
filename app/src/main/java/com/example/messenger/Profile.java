package com.example.messenger;

import static android.app.ProgressDialog.show;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.messenger.Authentication.Authorization;
import com.example.messenger.Authentication.MainActivity;
import com.example.messenger.Authentication.Registration;
import com.example.messenger.Model.Users;
import com.example.messenger.Reotrfit.Api;
import com.example.messenger.Reotrfit.RetrofitService;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Profile extends AppCompatActivity {

    private ImageButton btn_function_list, btn_add_photo, btn_change, btn_save, btn_chat,btn_add;
    private TextInputEditText et_phoneNumber;
    private EditText et_login;
    private CircleImageView image_photo;
    private Bitmap selectedImageBitmap;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private String photoUrlBD, loginBD, phoneNumber, newPhoneNumber, newphotoUrl, newLogin;
    private TextInputLayout textInputLayoutLogin;
    private boolean isChange;
    private RetrofitService retrofitService;
    private StorageReference storageRef;
    private List<String> allLogins;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private Dialog dialog;
    private EditText number1, number2, number3, number4, number5, number6;
    private Button btn_registration;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Инициализируем RetrofitService
        retrofitService = new RetrofitService();
        fetchExistingUserLogins();//Вызов метода для получение всех логинов из бд

        isChange = false;
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser(); // Получение текущего пользователя
        // Инициализация Firebase Storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        btn_function_list = findViewById(R.id.btn_function_list);
        btn_function_list.setOnClickListener(v -> {
            String[] actions = {"Выйти из аккаунта", "Удалить аккаунт", "Заблокированные контакты"};

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(Profile.this, android.R.layout.simple_list_item_1, actions) {
                @NonNull
                @Override
                public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    TextView textView = (TextView) super.getView(position, convertView, parent);
                    textView.setTextColor(Color.BLACK);

                    return textView;
                }
            };

            // Создаем AlertDialog и устанавливаем список действий
            AlertDialog.Builder builder = new AlertDialog.Builder(Profile.this);
            builder.setTitle(Html.fromHtml("<font color=\"" + R.color.white + "\">Список действий</font>"));
            builder.setIcon(R.drawable.icon);
            builder.setAdapter(adapter, (dialog, which) -> {
                switch (which) {
                    case 0:
                        //Выход из профиля
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(Profile.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                        break;
                    case 1:
                        //Удаление аккаунта
                        showConfirmationDialog();
                        break;
                    case 2:
                        // "Заблокированные контакты"
                        break;
                }
            });

            // Установка цвета фона на белый
            AlertDialog alertDialog = builder.create();
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));

            // Отображаем AlertDialog
            alertDialog.show();
        });

        //Диалоговое она с выбором изменения/добавления/удаления фотографии профиля
        btn_add_photo = findViewById(R.id.btn_add_photo);
        btn_add_photo.setOnClickListener(v->{
            if(isChange == true){
                Toast.makeText(Profile.this, "Завершите изменения и сохраните их!", Toast.LENGTH_SHORT).show();
            }else if (isChange == false){
                // Создаем AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(Profile.this);
                builder.setTitle("Выберите действие");
                // Цвет текста сообщения
                int blackColor = Color.BLACK;
                builder.setMessage(Html.fromHtml("<font color='" + blackColor + "'>Хотите изменить фото или удалить текущее изображение</font>"));
                builder.setPositiveButton("Изменить", (dialog, which) -> requestPermissionLauncher.launch("image/*"));
                builder.setNegativeButton("Удалить", (dialog, which) -> {
                    //В случае если мы удаляем фотографию профиля, то мы убираем его из хранилища и сохраняем изменения в бд
                    deletePhotoFromStorage(photoUrlBD);
                    saveUserData(currentUser, loginBD, null);
                    //Установка фото по умолчанию
                    image_photo.setImageResource(R.drawable.icon_user);
                });

                // Сохраняем AlertDialog для последующей настройки
                AlertDialog alertDialog = builder.create();

                // Настройка оформления AlertDialog
                Window window = alertDialog.getWindow();
                if (window != null) {
                    window.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
                }
                // Цвет текста заголовка
                int color1 = getResources().getColor(R.color.color1);
                alertDialog.setTitle(Html.fromHtml("<font color='" + color1 + "'>Выберите действие</font>"));
                // Цвет кнопок
                int color3 = getResources().getColor(R.color.color3);
                int color5 = getResources().getColor(R.color.color1);
                alertDialog.setOnShowListener(dialogInterface -> {
                    Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    positiveButton.setTextColor(color3);

                    Button negativeButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                    negativeButton.setTextColor(color5);
                });

                // Отображаем AlertDialog
                alertDialog.show();
            }
        });
        et_phoneNumber=findViewById(R.id.et_phoneNumber);
        et_phoneNumber.setEnabled(false);
        textInputLayoutLogin = findViewById(R.id.textInputLayoutLogin);
        et_login = textInputLayoutLogin.getEditText();
        et_login.setEnabled(false);

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
                    newLogin = s.toString();
                    //Проверка логина на соответствие условия и установка рамок
                    boolean isValid = isValidLogin(newLogin);

                    if (isValid) {
                        textInputLayoutLogin.setBoxStrokeColor(getResources().getColor(R.color.green));
                    } else {
                        textInputLayoutLogin.setBoxStrokeColor(getResources().getColor(R.color.red));
                    }
                }
            });
        }
        btn_save = findViewById(R.id.btn_save);
        btn_save.setVisibility(View.INVISIBLE);
        btn_save.setOnClickListener(v -> {
            saveUserData(currentUser, newLogin, photoUrlBD);
            et_login.setEnabled(false);
            loginBD=newLogin;
            isChange=false;
        });
        btn_change = findViewById(R.id.btn_change);
        btn_change.setOnClickListener(v -> {
            final Toast toast = Toast.makeText(getApplicationContext(), "Логин должен содержать символы a-z, 0-9, подчеркивание и не содержать пробелы." +
                    "Минимальная длина 5 символов.", Toast.LENGTH_SHORT);
            toast.show();
            isChange = true;
            et_login.setEnabled(true);
            et_login.requestFocus();
            et_phoneNumber.setEnabled(true);
            btn_save.setVisibility(View.VISIBLE);
        });
        btn_chat = findViewById(R.id.btn_chat);
        btn_chat.setOnClickListener(v->{
            Intent intent = new Intent(Profile.this, Chats.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });
        btn_add = findViewById(R.id.btn_add);
        image_photo = findViewById(R.id.image_photo_user);
        loadUserData();

    }

    //Метод получения данных пользователя
    private void loadUserData() {
        if (currentUser != null) {
            String userId = currentUser.getUid(); // Получаем ID текущего пользователя

            // Получаем Api сервис
            Api apiService = retrofitService.getRetrofit().create(Api.class);

            // Получаем данные профиля пользователя
            Call<Users> call = apiService.getProfileUser(userId);

            call.enqueue(new Callback<Users>() {
                @Override
                public void onResponse(Call<Users> call, Response<Users> response) {
                    if (response.isSuccessful()) {
                        // Получаем данные профиля пользователя
                        Users registeredUser = response.body();
                        if (registeredUser != null) {
                            // Извлекаем данные
                            loginBD = registeredUser.getLogin();
                            phoneNumber = registeredUser.getPhoneNumber();
                            photoUrlBD = String.valueOf(registeredUser.getImage_url());

                            et_login.setText(loginBD);
                            et_phoneNumber.setText(phoneNumber);

                            // Загружаем фотографию
                            Picasso.get()
                                    .load(photoUrlBD)
                                    .placeholder(R.drawable.icon_user)
                                    .error(R.drawable.icon_user)
                                    .into(image_photo);
                        }
                    } else {
                        Toast.makeText(Profile.this, "Ошибка получения данных", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Users> call, Throwable t) {
                    Toast.makeText(Profile.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(Profile.this, "Пользователь не аутентифицирован", Toast.LENGTH_SHORT).show();
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

    //Метод сохранения измененных данных пользователя
    private void saveUserData(FirebaseUser user, String login, String photoUrl) {
        if (user == null || (login != null && login.isEmpty()) || (photoUrl != null && photoUrl.isEmpty())) {
            Log.d("Валидация", "Данные имеют неправильный формат!");
            Toast.makeText(Profile.this, "Пожалуйста, заполните все обязательные поля.", Toast.LENGTH_SHORT).show();
            return;
        }

        newPhoneNumber = et_phoneNumber.getText().toString().trim();

        // Проверяем, отличается ли новый номер от номера из БД
        if (!phoneNumber.equals(newPhoneNumber)) {
            newPhoneNumber = "+" + newPhoneNumber.replaceAll("[^0-9]", "");

            // Проверяем, что номер не равен null и имеет правильный формат
            if (newPhoneNumber != null && newPhoneNumber.length() == 12) {
                checkPhoneNumberAndVerify(newPhoneNumber, user, login, photoUrl); // Проверяем и аутентифицируем номер
            } else {
                Toast.makeText(Profile.this, "Неверный формат номера телефона", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Если номер не изменился, просто обновляем другие данные
            updateUserProfile(user.getUid(), login, photoUrl, phoneNumber);
        }
    }

    // Метод проверки номера телефона и его аутентификации через Firebase
    private void checkPhoneNumberAndVerify(String newPhoneNumber, FirebaseUser user, String login, String photoUrl) {
        Api apiService = retrofitService.getRetrofit().create(Api.class);
        Call<Boolean> call = apiService.getPhoneNumber(newPhoneNumber);
        call.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful()) {
                    boolean exists = response.body();
                    if (exists) {
                        showPhoneNumberExistsDialog(newPhoneNumber);
                    } else {
                        // Если номер не существует, отправляем код на новый номер
                        showCodeInputDialog(newPhoneNumber, user, login, photoUrl);
                    }
                } else {
                    Toast.makeText(Profile.this, "Ошибка при проверке номера телефона", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Log.e("Ошибка", "Произошла ошибка при проверке номера телефона: " + t.getMessage());
                Toast.makeText(Profile.this, "Ошибка связи. Попробуйте еще раз.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Метод для обработки случая, когда номер телефона уже зарегистрирован за другим пользователем
    private void showPhoneNumberExistsDialog(String phoneNumber) {
        new AlertDialog.Builder(Profile.this)
                .setTitle("Номер занят")
                .setMessage("Номер " + phoneNumber + " уже зарегистрирован за другим пользователем. Вы можете:\n\n"
                        + "1. Войти в свой профиль, связанный с этим номером.\n"
                        + "2. Ввести другой номер телефона.")
                .setPositiveButton("Перейти к авторизации", (dialog, which) -> {
                    Intent intent = new Intent(Profile.this, Authorization.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                })
                .setNegativeButton("Сменить номер", (dialog, which) -> {
                    dialog.dismiss();
                    et_phoneNumber.requestFocus();
                })
                .show();
    }

    // Открытие диалога для ввода кода
    private void showCodeInputDialog(String phoneNumber, FirebaseUser user, String login, String photoUrl) {
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.activity_code);

        // Инициализация элементов диалога для ввода кода
        number1 = dialog.findViewById(R.id.number1);
        number2 = dialog.findViewById(R.id.number2);
        number3 = dialog.findViewById(R.id.number3);
        number4 = dialog.findViewById(R.id.number4);
        number5 = dialog.findViewById(R.id.number5);
        number6 = dialog.findViewById(R.id.number6);
        btn_registration = dialog.findViewById(R.id.btn_registration);

        // Инициализируем mCallbacks ДО вызова verifyPhoneNumber
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                // Автоматическое завершение верификации
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                handleVerificationError(e);
            }

            @Override
            public void onCodeSent(@NonNull String verification, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                dialog.show();
                setUpCodeEntry(verification, phoneNumber, user, login, photoUrl);
            }
        };

        // Затем вызываем verifyPhoneNumber
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                new PhoneAuthOptions.Builder(FirebaseAuth.getInstance())
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks) // mCallbacks уже инициализирован
                        .build()
        );

        dialog.show();
    }

    //Метод для обработки ошибок аутентификации с Firebase
    private void handleVerificationError(FirebaseException e) {
        Toast.makeText(Profile.this, "Ошибка верификации номера телефона: " + e.getMessage(), Toast.LENGTH_SHORT).show();

        if (e instanceof FirebaseAuthInvalidCredentialsException) {
            // Некорректный формат номера телефона
            Toast.makeText(Profile.this, "Некорректный формат номера телефона", Toast.LENGTH_SHORT).show();
        } else if (e instanceof FirebaseTooManyRequestsException) {
            // Превышение лимита запросов на верификацию
            Toast.makeText(Profile.this, "Превышен лимит запросов на верификацию. Попробуйте позже", Toast.LENGTH_SHORT).show();
        } else {
            // Другие типы ошибок
            Toast.makeText(Profile.this, "Произошла ошибка верификации номера телефона", Toast.LENGTH_SHORT).show();
        }
    }

    // Установка обработчика для ввода кода
    private void setUpCodeEntry(String verificationId, String newPhoneNumber, FirebaseUser user, String login, String photoUrl) {
        setEditTextAutoAdvance(number1, number2);
        setEditTextAutoAdvance(number2, number3);
        setEditTextAutoAdvance(number3, number4);
        setEditTextAutoAdvance(number4, number5);
        setEditTextAutoAdvance(number5, number6);

        btn_registration.setOnClickListener(v -> {
            String inputCode = number1.getText().toString() + number2.getText().toString() +
                    number3.getText().toString() + number4.getText().toString() +
                    number5.getText().toString() + number6.getText().toString();

            if (TextUtils.isEmpty(inputCode)) {
                Toast.makeText(Profile.this, "Введите полученный код", Toast.LENGTH_SHORT).show();
            } else {
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, inputCode);
                signInWithPhoneAuthCredential(credential, newPhoneNumber, user, login, photoUrl);
            }
        });
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
    // Метод для завершения аутентификации
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential, String newPhoneNumber, FirebaseUser user, String login, String photoUrl) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Успешная аутентификация, обновляем номер в Firebase Auth
                        user.updatePhoneNumber(credential)
                                .addOnCompleteListener(updateTask -> {
                                    if (updateTask.isSuccessful()) {
                                        // Обновляем профиль с новым номером телефона
                                        updateUserProfile(user.getUid(), login, photoUrl, newPhoneNumber);
                                    } else {
                                        Toast.makeText(Profile.this, "Ошибка при обновлении номера в Firebase", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(Profile.this, "Ошибка аутентификации с помощью SMS", Toast.LENGTH_SHORT).show();
                    }
                });
        dialog.dismiss();

    }

    // Метод обновления профиля пользователя с возможностью обновить номер телефона
    private void updateUserProfile(String userId, String login, String photoUrl, String newPhoneNumber) {
        Users updatedUser = new Users();
        updatedUser.setLogin(login);
        updatedUser.setImage_url(photoUrl);

        if (newPhoneNumber != null) {
            updatedUser.setPhoneNumber(newPhoneNumber);
        }

        Api api = retrofitService.getRetrofit().create(Api.class);
        Call<Users> call = api.updateUserProfile(userId, updatedUser);

        call.enqueue(new Callback<Users>() {
            @Override
            public void onResponse(Call<Users> call, Response<Users> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(Profile.this, "Профиль успешно обновлен", Toast.LENGTH_SHORT).show();
                    et_login.setEnabled(false);
                    et_phoneNumber.setEnabled(false);
                    btn_save.setVisibility(View.INVISIBLE);
                } else {
                    Log.d("Ошибка", "Не удалось обновить профиль: " + response.code());
                    Toast.makeText(Profile.this, "Не удалось обновить профиль. Попробуйте еще раз.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Users> call, Throwable t) {
                Log.d("Ошибка сети", t.getMessage());
                Toast.makeText(Profile.this, "Ошибка сети. Проверьте подключение и попробуйте снова.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri result) {
                    try {
                        selectedImageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), result);
                        image_photo.setImageBitmap(selectedImageBitmap);
                        //Если у пользоваетеля уже есть фото, то сначала мы удаляем старое
                        if (photoUrlBD != null){
                            deletePhotoFromStorage(photoUrlBD);
                        }
                        //А затем меняем на новое
                        uploadPhotoToStorage(currentUser);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Изображение не выбрано!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    // Метод установки
    private void uploadPhotoToStorage(FirebaseUser user) {
        if (selectedImageBitmap != null) {
            //Конвертируем изображение
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            String imagePath = "users_profile_image/" + user.getUid() + ".jpg"; // Сохраняем путь к изображению
            StorageReference imageRef = storageRef.child(imagePath);
            //Сохраняем изображение в хранилище
            UploadTask uploadTask = imageRef.putBytes(data);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    //В случае если выбрано новое изображение, то берем ссылку и отправляем ее с данными ползователя на сохранение
                    newphotoUrl = uri.toString();
                    saveUserData(user, loginBD, newphotoUrl);
                    photoUrlBD=newphotoUrl;
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(Profile.this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show();
            });
        } else {
            //В случае если пользователь не выбирает изображение, то мы вносим изменения в бд
            saveUserData(user, loginBD, null);
        }
    }

    //Удаление фото из хранилища
    private void deletePhotoFromStorage(String photoUrl) {
        if (photoUrl != null) {
            // Получить ссылку на файл, который нужно удалить
            StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(photoUrl);

            // Удалить файл из Firebase Storage
            photoRef.delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(Profile.this, "Изображение удалено!", Toast.LENGTH_SHORT).show();
                        photoUrlBD = null;
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(Profile.this, "Ошибка при удалении старого изображения из хранилища: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    //Окно для подтверждения удаления
    private void showConfirmationDialog() {
        AlertDialog.Builder confirmationDialog = new AlertDialog.Builder(Profile.this);

        int color1 = getResources().getColor(R.color.color1);
        int colorBlack = Color.BLACK;
        int colorGrey = Color.GREEN;
        int colorRed = Color.RED;

        confirmationDialog.setTitle(Html.fromHtml("<font color='" + color1 + "'>Удаление аккаунта</font>"));
        confirmationDialog.setMessage(Html.fromHtml("<font color='" + colorBlack + "'>Вы уверены, что хотите удалить аккаунт?</font>"));

        confirmationDialog.setPositiveButton("Да", (dialog, which) -> {
            deleteAccount();
        });
        confirmationDialog.setNegativeButton("Нет", (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog alertDialog = confirmationDialog.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));

        alertDialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            positiveButton.setTextColor(colorRed);

            Button negativeButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
            negativeButton.setTextColor(colorGrey);
        });
        alertDialog.show();
    }

    //Метод удаление профиля пользователя из бд через сервер
    private void deleteAccount() {
        // Удаляем изображение из хранилища Firebase, если оно есть
        if (photoUrlBD != null){
            deletePhotoFromStorage(photoUrlBD);
        }

        // Удаление профиля из вашего API
        Api api = retrofitService.getRetrofit().create(Api.class);
        Call<String> call = api.deleteProfileUser(currentUser.getUid());

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    // Успех удаления профиля из вашего API, теперь удаляем аккаунт из Firebase
                    deleteFirebaseAccount();
                } else {
                    Toast.makeText(Profile.this, "Ошибка при удалении профиля. Попробуйте снова.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(Profile.this, "Ошибка при удалении профиля. Попробуйте снова.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    //Метод удаление профиля пользователя в firebase autho
    private void deleteFirebaseAccount() {
        if (currentUser != null) {
            currentUser.delete().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(Profile.this, "Аккаунт успешно удален!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Profile.this, MainActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                } else {
                    Toast.makeText(Profile.this, "Ошибка при удалении аккаунта. Попробуйте снова.", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(Profile.this, "Ошибка при удалении аккаунта: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(Profile.this, "Нет активного пользователя для удаления", Toast.LENGTH_SHORT).show();
        }
    }

}