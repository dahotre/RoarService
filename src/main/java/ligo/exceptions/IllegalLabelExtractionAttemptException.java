package ligo.exceptions;

/**
 * Attempted to extract label from a class which is not an @Entity
 */
public class IllegalLabelExtractionAttemptException extends RuntimeException {
  public IllegalLabelExtractionAttemptException() {}

  public IllegalLabelExtractionAttemptException(String message) {
    super(message);
  }
}
