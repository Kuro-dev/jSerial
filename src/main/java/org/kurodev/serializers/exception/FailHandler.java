package org.kurodev.serializers.exception;

public interface FailHandler<T extends Throwable> {
    FailHandler<Exception> WRAP_IN_RUNTIME_EX = ex -> {
        throw new RuntimeException(ex);
    };
    FailHandler<Exception> PRINT_STACK_TRACE = Throwable::printStackTrace;

    FailHandler<Exception> IGNORE = ex -> {
    };

    void onException(T ex);
}
