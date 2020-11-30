package socket.commons.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import socket.commons.enums.Action;
import socket.commons.enums.Language;

@Getter
public class ServerPublicKeyRequest extends Request {
    @Builder
    public ServerPublicKeyRequest(@NonNull Action action) {
        super(action);
    }
}
