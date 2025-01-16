package runtime;

import doctor.RuntimeError;
import scanner.Token;

import java.util.HashMap;
import java.util.Map;

class Environment {
    private final Map<String, Object> values = new HashMap<>();

    void define(Token name, Object value) {
        values.put(name.lexeme(), value);
    }

    Object get(Token name) {
        if (!values.containsKey(name.lexeme())) {
            throw new RuntimeError(name, "Undefined variable '%s'".formatted(name.lexeme()));
        }
        return values.get(name.lexeme());
    }
}
