package com.example.jennya.mytravel;

import android.os.StrictMode;
import android.util.Log;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

import java.io.IOException;

public class Translator
{
    private static final String TAG = Translator.class.getCanonicalName();

    @SuppressWarnings("SpellCheckingInspection")
    private static final String key = "trnsl.1.1.20170110T150740Z.4661e76fe1f5d8ae.4505da4a8e37d2437f478eaff89ebbc46372f9bc";


    private final YandexTranslateService yandexTranslateService;
    private final String targetLanguage;

    public Translator(String targetLanguage)
    {
        this.targetLanguage = targetLanguage;
        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://translate.yandex.net")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

        yandexTranslateService = retrofit.create(YandexTranslateService.class);
    }

    public String translate(String text)
    {
        Call<YandexTranslateResult> translateCall = yandexTranslateService.translate(key, text, targetLanguage);

        Response<YandexTranslateResult> translateResponse;

        try
        {
            translateResponse = translateCall.execute();
        }
        //server did not respond
        catch (IOException e)
        {
            Log.e(TAG, "Cannot call Yandex Translate Service", e);
            return text;
        }

        //server responded with error msg
        if (!translateResponse.isSuccessful())
        {
            Log.e(TAG, "Call to Yandex Translate Service was unsuccessful");
            return text;
        }

        YandexTranslateResult translateResult = translateResponse.body();

        return translateResult.text[0];
    }
}



interface YandexTranslateService
{
    @GET("api/v1.5/tr.json/translate")
    Call<YandexTranslateResult> translate(@Query("key" ) String key,
                                          @Query("text") String text,
                                          @Query("lang") String targetLang);
}


class YandexTranslateResult
{
    public int      code;
    public String   lang;
    public String[] text;
}


// https://translate.yandex.net/api/v1.5/tr.json/translate?key=trnsl.1.1.20170110T150740Z.4661e76fe1f5d8ae.4505da4a8e37d2437f478eaff89ebbc46372f9bc&text=%D0%B4%D0%BE%D0%BC&lang=en&