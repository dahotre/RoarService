package ligo.exceptions;

/**
 * Combines reflection related exceptions into 1
 */
public class IllegalReflectionOperation extends RuntimeException {
  public IllegalReflectionOperation(Throwable cause) {
    super(cause);
  }

  public IllegalReflectionOperation(String message, Throwable cause) {
    super(message, cause);
  }

  public IllegalReflectionOperation(String message) {
    super(message);
  }
}
