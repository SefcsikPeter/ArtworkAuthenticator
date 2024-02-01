package artwork.authenticator.dto;

import artwork.authenticator.type.Artist;

public record ResultListDto(
    Long resultId,
    Long artworkId,
    Artist artist,
    String artworkTitle,
    String neuralNetResult,
    String gptResult
) {
}
