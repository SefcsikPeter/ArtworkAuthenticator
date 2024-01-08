package artwork.authenticator.dto;

import artwork.authenticator.type.Sex;
import java.time.LocalDate;

public record HorseDetailDto(
    Long id,
    String name,
    String description,
    LocalDate dateOfBirth,
    Sex sex,
    OwnerDto owner
) {
  public HorseDetailDto withId(long newId) {
    return new HorseDetailDto(
        newId,
        name,
        description,
        dateOfBirth,
        sex,
        owner);
  }

  public Long ownerId() {
    return owner == null
        ? null
        : owner.id();
  }

}
