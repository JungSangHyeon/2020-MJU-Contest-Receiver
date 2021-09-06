package com.example.multion_offkeyingprototype;

import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Connect {

    public void sendData2Server(String playerID, String signal) {
        OkHttpClient client = new OkHttpClient();
        client.newCall(
                new Request.Builder()
                        .url("http://3.35.0.42:8080/user/change")
                        .post(
                                new MultipartBody.Builder()
                                        .setType(MultipartBody.FORM)
                                        .addFormDataPart("userID", playerID)
                                        .addFormDataPart("signal", signal)
                                        .build()
                ).build()
        ).enqueue(new Callback() {
            @Override public void onFailure(@NotNull Call call, @NotNull IOException e) { e.printStackTrace(); }
            @Override public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException { Log.d("Hello!", response.body().string()); }
        });
    }
}
