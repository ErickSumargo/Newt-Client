package app.newt.id.helper;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import app.newt.id.R;
import app.newt.id.server.interfaces.REST;
import app.newt.id.server.model.User;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Erick Sumargo on 2/27/2018.
 */

public class API {
    private REST rest;

    private API(final Context context) {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        httpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Interceptor.Chain chain) throws IOException {
                String userType = Session.with(context).getUserType();
                userType = !userType.isEmpty() ? userType : Constant.GUEST;

                String token = Session.with(context).getToken();
                token = !token.isEmpty() ? token : Constant.JWT_TOKEN;

                String header = String.format("%s %s %s", context.getString(R.string.app_name), userType, token);

                Request original = chain.request();
                Request.Builder requestBuilder = original.newBuilder().header(Constant.AUTHORIZATION, header);
                Request request = requestBuilder.build();

                return chain.proceed(request);
            }
        });

        Gson gson = new GsonBuilder()
                .setLenient()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .create();

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClient.addInterceptor(logging);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constant.URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(httpClient.build())
                .build();
        rest = retrofit.create(REST.class);
    }

    public static API with(Context context) {
        return new API(context);
    }

    public REST getRest() {
        return rest;
    }
}