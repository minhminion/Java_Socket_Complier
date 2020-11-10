package socket.commons.response;

import socket.commons.enums.StatusCode;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CompileResponse extends Response{
    private String code;
    private String output;

    @Builder
    public CompileResponse(StatusCode statusCode, String code, String output) {
        this.statusCode = statusCode;
        this.code = code;
        this.output = output;
    }
}
