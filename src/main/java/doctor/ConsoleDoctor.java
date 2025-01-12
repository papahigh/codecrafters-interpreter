package doctor;

final class ConsoleDoctor implements Doctor {

    private boolean hasErrors;

    @Override
    public boolean hasErrors() {
        return hasErrors;
    }

    @Override
    public void report(int line, String where, String message) {
        var content = "[line %s] Error%s: %s".formatted(line, where, message);
        System.err.println(content);
        hasErrors = true;
    }
}
