package doctor;

final class ConsoleDoctor implements Doctor {

    private boolean hasErrors;

    @Override
    public void diagnostics() {
        if (hasErrors) {
            // https://man.freebsd.org/cgi/man.cgi?query=sysexits&apropos=0&sektion=0&manpath=FreeBSD+4.3-RELEASE&format=html
            System.exit(65);
        }
    }

    @Override
    public void report(int line, String where, String message) {
        var content = "[line %s] Error%s: %s".formatted(line, where, message);
        System.err.println(content);
        hasErrors = true;
    }
}
