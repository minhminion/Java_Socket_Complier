package socket.commons.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import socket.commons.enums.Action;

@Getter
public class SendClientSecretKeyRequest extends Request {
    private String clientSecretKey;

    @Builder
    public SendClientSecretKeyRequest(@NonNull Action action, String clientSecretKey) {
        super(action);
        this.clientSecretKey = clientSecretKey;
    }

    public String getClientSecretKey() {
        return clientSecretKey;
    }

    public void setClientSecretKey(String clientSecretKey) {
        this.clientSecretKey = clientSecretKey;
    }
}
