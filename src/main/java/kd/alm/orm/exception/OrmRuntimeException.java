package kd.alm.orm.exception;

/**
 * </p>
 * ORM 运行时异常
 * <p>
 *
 * @author chenquan chenquan.dev@foxmail.com 2021/05/08 11:30
 */
public class OrmRuntimeException extends RuntimeException {
    public OrmRuntimeException() {
    }

    public OrmRuntimeException(String message) {
        super(message);
    }

    public OrmRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public OrmRuntimeException(Throwable cause) {
        super(cause);
    }

    public OrmRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
