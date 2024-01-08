package artwork.authenticator.dto;

import artwork.authenticator.type.Artist;

public record ArtworkDetailDto(
    Long id,
    String title,
    Artist artist,
    String gallery,
    String price,
    String description,
    String image
) {

}
