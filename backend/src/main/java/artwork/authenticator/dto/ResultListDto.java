package artwork.authenticator.dto;

public record ResultListDto(
    Long resultId,
    Long artworkId,
    String artworkTitle,
    String neuralNetResult,
    String gptResult
) {
}
