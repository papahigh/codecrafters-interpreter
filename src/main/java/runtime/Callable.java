package runtime;

import parser.Statement;

import java.util.List;

public interface Callable {

    default int length() {
        return 0;
    }

    Object call(Runtime runtime, List<Object> args);

    class DefaultCallable implements Callable {

        private final Statement.FunctionStatement function;
        private final Environment closure;

        DefaultCallable(Statement.FunctionStatement function, Environment closure) {
            this.function = function;
            this.closure = closure;
        }

        public int length() {
            return function.parameters().size();
        }

        @Override
        public Object call(Runtime runtime, List<Object> args) {
            try {
                var environment = closure.fork();
                for (int i = 0; i < function.parameters().size(); i++) {
                    environment.define(function.parameters().get(i).lexeme(), args.get(i));
                }
                runtime.executeBlock(function.body(), environment);
            } catch (Return it) {
                return it.value;
            }
            return null;
        }

        @Override
        public String toString() {
            return "<fn " + function.name().lexeme() + ">";
        }
    }

    class Return extends RuntimeException {
        private final Object value;

        Return(Object value) {
            super(null, null, false, false);
            this.value = value;
        }
    }
}
