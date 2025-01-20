package runtime;

import parser.Statement;
import scanner.Token;

import java.util.List;

import static java.util.Optional.ofNullable;

public interface Callable {

    default int length() {
        return 0;
    }

    Object call(Runtime runtime, List<Object> args);

    class DefaultCallable implements Callable {

        private final Token name;
        private final List<Token> params;
        private final List<Statement> body;
        private final Environment closure;

        public DefaultCallable(Token name, List<Token> params, List<Statement> body, Environment closure) {
            this.name = name;
            this.params = params;
            this.body = body;
            this.closure = closure;
        }

        public int length() {
            return params.size();
        }

        @Override
        public Object call(Runtime runtime, List<Object> args) {
            try {
                var environment = closure.fork();
                for (int i = 0; i < params.size(); i++) {
                    environment.define(params.get(i).lexeme(), args.get(i));
                }
                runtime.executeBlock(body, environment);
            } catch (Return it) {
                return it.value;
            }
            return null;
        }

        @Override
        public String toString() {
            return "<fn %s>".formatted(ofNullable(name).map(Token::lexeme).orElse("anonymous"));
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
