package com.example.codeblockmobileclient.communication;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonSyntaxException;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public class HttpRequest<T> extends Request<T> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Class<T> _class;
    private final Map<String, String> headers;
    private final Response.Listener<T> listener;
    private final Response.ErrorListener errorListener;

    public HttpRequest(int method, String url, Class<T> _class, Map<String, String> headers,
                       @Nullable Response.Listener<T> listener, @Nullable Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this._class = _class;
        this.headers = headers;
        this.listener = listener;
        this.errorListener = errorListener;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers != null ? headers : super.getHeaders();
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            T obj = objectMapper.readValue(json, objectMapper.getTypeFactory().constructParametricType(Response.class, _class));
            return Response.success(obj, HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException | JsonSyntaxException e) {
            return Response.error(new ParseError(e));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void deliverResponse(T response) {
        listener.onResponse(response);
    }
}
