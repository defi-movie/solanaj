package org.p2p.solanaj.core;

public class AccountNotFound extends RuntimeException {

    public AccountNotFound() {
        super();
    }

    public AccountNotFound(String message) {
        super(message);
    }

    public AccountNotFound(String message, Throwable cause) {
        super(message, cause);
    }

    public AccountNotFound(Throwable cause) {
        super(cause);
    }

    protected AccountNotFound(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
