package artwork.authenticator.rest;

import java.util.List;

public record ValidationErrorRestDto(
    String message,
    List<String> errors
) {
}
