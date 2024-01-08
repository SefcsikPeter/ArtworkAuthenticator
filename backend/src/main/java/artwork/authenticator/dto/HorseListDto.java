package artwork.authenticator.dto;

import artwork.authenticator.type.Sex;
import java.time.LocalDate;

/**
 * Class for Horse DTOs
 * Contains all common properties
 */
public record HorseListDto(
    Long id,
    String name,
    String description,
    LocalDate dateOfBirth,
    Sex sex,
    OwnerDto owner
) {
  public Long ownerId() {
    return owner == null
        ? null
        : owner.id();
  }
}
