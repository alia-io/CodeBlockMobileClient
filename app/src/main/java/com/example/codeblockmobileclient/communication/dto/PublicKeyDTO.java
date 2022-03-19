package com.example.codeblockmobileclient.communication.dto;

import com.google.crypto.tink.CleartextKeysetHandle;
import com.google.crypto.tink.JsonKeysetReader;
import com.google.crypto.tink.KeysetHandle;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;

import lombok.Data;

@Data
public class PublicKeyDTO {

    private byte[] encodedPublicKeySetHandle;

    public KeysetHandle getPublicKeySetHandle() throws IOException, GeneralSecurityException {
        ByteArrayInputStream is = new ByteArrayInputStream(encodedPublicKeySetHandle);
        return CleartextKeysetHandle.read(JsonKeysetReader.withInputStream(is));
    }
}
