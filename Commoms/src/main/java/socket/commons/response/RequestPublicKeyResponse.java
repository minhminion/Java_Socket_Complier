package socket.commons.response;

import lombok.Builder;
import socket.commons.enums.StatusCode;

public class RequestPublicKeyResponse extends Response {
    private byte[] publicKeyBytes;

    @Builder
    public RequestPublicKeyResponse(byte[] publicKeyBytes, StatusCode statusCode) {
        this.publicKeyBytes = publicKeyBytes;
        this.statusCode = statusCode;
    }

    public byte[] getPublicKeyBytes() {
        return publicKeyBytes;
    }

    public void setPublicKeyBytes(byte[] publicKeyBytes) {
        this.publicKeyBytes = publicKeyBytes;
    }
}
