package artwork.authenticator.dto;

public record OwnerDto(
    long id,
    String firstName,
    String lastName,
    String email
) {
}
