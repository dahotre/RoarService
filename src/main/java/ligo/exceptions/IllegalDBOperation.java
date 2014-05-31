package ligo.exceptions;

/**
 * Wrapper exception for anything wrong in DB operations
 */
public class IllegalDBOperation extends RuntimeException {

  public IllegalDBOperation(String message) {
    super(message);
  }

  public IllegalDBOperation(String message, Throwable cause) {
    super(message, cause);
  }
}
