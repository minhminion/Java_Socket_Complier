package socket.commons.request;

import socket.commons.enums.Action;
import socket.commons.enums.Language;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class CompileRequest extends Request{

    private Language language;
    private String code;

    @Builder
    public CompileRequest(@NonNull Action action, Language language, String code) {
        super(action);
        this.language = language;
        this.code = code;
    }
}
