package com.complier.socket.commons.request;

import com.complier.socket.commons.enums.Action;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class InformationRequest extends Request{
    @Builder
    public InformationRequest (@NonNull Action action) {
        super(action);
    }
}
