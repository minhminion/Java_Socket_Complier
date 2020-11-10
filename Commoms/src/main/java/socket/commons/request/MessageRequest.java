package socket.commons.request;

import socket.commons.enums.Action;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class MessageRequest extends Request{
    private String message;

    @Builder
    public MessageRequest(@NonNull Action action, String message) {
        super(action);
        this.message = message;
    }

}
