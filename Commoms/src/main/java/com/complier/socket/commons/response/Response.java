package com.complier.socket.commons.response;

import com.complier.socket.commons.enums.StatusCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

@Getter
public abstract class Response implements Serializable {

    protected StatusCode statusCode;
}
