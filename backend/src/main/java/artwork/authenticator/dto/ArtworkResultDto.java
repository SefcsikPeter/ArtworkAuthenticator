package artwork.authenticator.dto;

public record ArtworkResultDto(
    Long artworkId,
    String neuralNetResult,
    String gptResult
) {
}
