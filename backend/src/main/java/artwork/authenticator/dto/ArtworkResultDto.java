package artwork.authenticator.dto;

public record ArtworkResultDto(
    Long id,
    Long artworkId,
    String neuralNetResult,
    String gptResult
) {
}
