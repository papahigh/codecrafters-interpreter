package runtime;

import doctor.RuntimeError;
import scanner.Token;

import java.util.HashMap;
import java.util.Map;

class Environment {
    private final Map<String, Object> values = new HashMap<>();
    private final Environment parent;

    Environment(Environment parent) {
        this.parent = parent;
    }

    Environment() {
        parent = null;
    }

    void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme()))
            values.put(name.lexeme(), value);
        else if (parent != null)
            parent.assign(name, value);
        else throw new RuntimeError(name, "Undefined variable '%s'".formatted(name.lexeme()));
    }

    void define(Token name, Object value) {
        values.put(name.lexeme(), value);
    }

    Object get(Token name) {
        if (values.containsKey(name.lexeme()))
            return values.get(name.lexeme());
        if (parent != null)
            return parent.get(name);

        throw new RuntimeError(name, "Undefined variable '%s'".formatted(name.lexeme()));
    }
}
