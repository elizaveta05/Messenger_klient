package com.example.messenger.Authentication.Class;

import android.util.Log;

import com.example.messenger.Reotrfit.Api;
import com.example.messenger.Reotrfit.RetrofitService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetUsersLogin {
    private RetrofitService retrofitService = new RetrofitService();

    // Метод получения всех логинов существующих пользователей
    public void getUsersLogin(final UsersLoginCallback callback) { // Добавлен колбек
        Api apiService = retrofitService.getRetrofit().create(Api.class);

        Call<List<String>> call = apiService.getUsersLogin();
        call.enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Если запрос успешен и тело ответа не пустое
                    List<String> allLogins = response.body();
                    Log.d("FetchLogins", "Логины успешно получены: " + allLogins);
                    callback.onSuccess(allLogins); // Вызов колбека
                } else {
                    // Если ответ не успешен
                    String errorMessage = response.errorBody() != null ? response.errorBody().toString() : "Unknown Error";
                    Log.e("FetchLogins", "Ошибка получения логинов: " + errorMessage);
                    callback.onError(errorMessage); // Вызов колбека с ошибкой
                }
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                Log.e("FetchLogins", "Ошибка сети: " + t.getMessage());
                callback.onError(t.getMessage()); // Вызов колбека с ошибкой
            }
        });
    }

    // Интерфейс для обратного вызова
    public interface UsersLoginCallback {
        void onSuccess(List<String> logins);
        void onError(String errorMessage);
    }
}
