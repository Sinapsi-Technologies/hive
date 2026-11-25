package ${packageName};

public class ${className} extends RuntimeException {

${defaultConstructor}    public ${className}(String message) {
        super(message);
    }

    public ${className}(String message, Throwable cause) {
        super(message, cause);
    }
}
