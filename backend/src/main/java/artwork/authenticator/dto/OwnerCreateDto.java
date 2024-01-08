package artwork.authenticator.dto;

public record OwnerCreateDto(
    String firstName,
    String lastName,
    String email
) {
}
