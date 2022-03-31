package com.example.codeblockmobileclient.communication;

import android.util.Log;

import androidx.annotation.Nullable;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public class HttpRequest<T> extends JsonRequest<T> {

    private static final Gson gson = new Gson();
    private final Class clazz;
    private final int method;
    private final String url;
    private String requestBody;
    private Map<String, String> headers;
    private Map<String, Object> params;
    private final Response.Listener<T> listener;
    private final Response.ErrorListener errorListener;

    public HttpRequest(int method, String url, Map<String, Object> params, Class clazz,
                       Response.Listener<T> listener, @Nullable Response.ErrorListener errorListener) {
        super(method, url, gson.toJson(params), listener, errorListener);
        this.clazz = clazz;
        this.method = method;
        this.url = url;
        this.listener = listener;
        this.errorListener = errorListener;
        if (params != null) {
            this.params = params;
            this.requestBody = gson.toJson(params);
        }
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            return (Response<T>) Response.success(
                    gson.fromJson(
                            new String(response.data, HttpHeaderParser.parseCharset(response.headers)), clazz),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException | JsonSyntaxException e) {
            Log.d("HttpRequest", e.getMessage());
            return Response.error(new ParseError(e));
        }
    }

//    @Override
//    protected Response<T> parseNetworkResponse(NetworkResponse response) {
//        try {
//            String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
//            // T obj = objectMapper.readValue(json, objectMapper.getTypeFactory().constructParametricType(Response.class, _class));
//            // return Response.success(obj, HttpHeaderParser.parseCacheHeaders(response));
//        } catch (UnsupportedEncodingException | JsonSyntaxException e) {
//            return Response.error(new ParseError(e));
//        // } catch (JsonProcessingException e) {
//        //     e.printStackTrace();
//        }
//
//        return null;
//    }

    @Override
    protected void deliverResponse(T response) {
        listener.onResponse(response);
    }
}
