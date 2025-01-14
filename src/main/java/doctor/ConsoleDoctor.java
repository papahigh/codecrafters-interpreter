package doctor;

final class ConsoleDoctor implements Doctor {

    private boolean hasErrors;
    private boolean hasRuntimeError;

    /**
     * {@see  <a href="https://man.freebsd.org/cgi/man.cgi?query=sysexits&apropos=0&sektion=0&manpath=FreeBSD+4.3-RELEASE&format=html">Preferable exit codes</a>}
     */
    @Override
    public void diagnostics() {
        if (hasErrors) System.exit(65);
        if (hasRuntimeError) System.exit(70);
    }

    @Override
    public void report(int line, String where, String message) {
        var content = "[line %s] Error%s: %s".formatted(line, where, message);
        System.err.println(content);
        hasErrors = true;
    }

    @Override
    public void runtimeError(RuntimeError error) {
        var content = "%s\n[line %s]".formatted(error.getMessage(), error.token.line());
        System.err.println(content);
        hasRuntimeError = true;
    }
}
