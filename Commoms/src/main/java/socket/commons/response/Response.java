package socket.commons.response;

import socket.commons.enums.StatusCode;
import lombok.Getter;

import java.io.Serializable;

@Getter
public abstract class Response implements Serializable {

    protected StatusCode statusCode;
}
