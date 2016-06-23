package com.microsoft.projectoxford.emotion.rest;

import com.google.gson.Gson;
import com.microsoft.projectoxford.emotion.contract.RecognizeResult;

import java.util.List;
import java.util.Map;


import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.converter.gson.GsonConverterFactory;
/**
 * Created by jincui on 6/21/16.
 */
public class WebServiceRequestRetrofit {
    private String subscriptionKey;
    private static final String headerKey = "ocp-apim-subscription-key";
    private Gson gson = new Gson();

    public class EmotionRequestJson{
        public String url;
        public EmotionRequestJson(String url){
            this.url = url;

        }
    }
    public interface EmotionService{
        @POST("emotion/v1.0/recognize")
        @Headers({
                "Content-Type: application/json",
                "Host: api.projectoxford.ai",
        })
        Call<List<RecognizeResult>> emotionRequest(@Header(headerKey) String subkey, @Body EmotionRequestJson reqBodyJson);

        @POST("emotion/v1.0/recognize")
        @Headers({
                "Content-Type: application/octet-stream",
                "Host: api.projectoxford.ai",
        })
        Call<List<RecognizeResult>> emotionRequest(@Header(headerKey) String subkey, @Body RequestBody reqBody);


    }


    public WebServiceRequestRetrofit(String key) {
        this.subscriptionKey = key;
    }

    public List<RecognizeResult> post(String url, Map<String, Object> data, String contentType, boolean responseInputStream) throws EmotionServiceException {

        List<RecognizeResult> results = null;
        boolean isStream = false;
        EmotionRequestJson body;

        /*Set header*/
        if (contentType != null && !contentType.isEmpty()) {

            if (contentType.toLowerCase().contains("octet-stream")) {
                isStream = true;
            }
        }


        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        // set your desired log level
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        // add your other interceptors …
        // add logging as last interceptor
        httpClient.addInterceptor(logging);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.projectoxford.ai/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        EmotionService emotionService = retrofit.create(EmotionService.class);
        try {
            if (!isStream) {

                body = new EmotionRequestJson((String)data.get("url"));

                Call<List<RecognizeResult>> call = emotionService.emotionRequest(subscriptionKey, body);
                results = call.execute().body();

            } else {
                byte[] image = ((byte[]) data.get("data"));
                RequestBody requestBody = RequestBody
                        .create(MediaType.parse("application/octet-stream"), image);
                Call<List<RecognizeResult>> call = emotionService.emotionRequest(subscriptionKey, requestBody);
                results = call.execute().body();
            }

        } catch (Exception e) {
            throw new EmotionServiceException(e.getMessage());
        }

        return results;

    }

}
