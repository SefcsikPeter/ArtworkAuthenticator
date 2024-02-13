package artwork.authenticator.dto;

public record MessageListDto(
    String userMessage,
    String gptResponse
) {
}
