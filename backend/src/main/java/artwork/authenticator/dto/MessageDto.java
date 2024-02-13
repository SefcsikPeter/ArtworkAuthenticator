package artwork.authenticator.dto;

public record MessageDto(
    Long resultId,
    String userMessage,
    String gptResponse
) {
}
