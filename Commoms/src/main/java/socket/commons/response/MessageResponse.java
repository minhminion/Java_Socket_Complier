package socket.commons.response;

import socket.commons.enums.StatusCode;
import lombok.Builder;
import lombok.Getter;

@Getter
public class MessageResponse extends Response{
    private String message;

    @Builder
    public MessageResponse(String message, StatusCode statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }
}
