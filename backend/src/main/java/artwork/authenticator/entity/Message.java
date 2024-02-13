package artwork.authenticator.entity;

public class Message {
  private Long id;
  private Long resultId;
  private String userMessage;
  private String gptResponse;

  public Long getId() {
    return id;
  }
  public Message setId(Long id) {
    this.id = id;
    return this;
  }
  public Long getResultId() {
    return resultId;
  }

  public Message setResultId(Long resultId) {
    this.resultId = resultId;
    return this;
  }

  public String getUserMessage() {
    return userMessage;
  }

  public Message setUserMessage(String userMessage) {
    this.userMessage = userMessage;
    return this;
  }

  public String getGptResponse() {
    return gptResponse;
  }

  public Message setGptResponse(String gptResult) {
    this.gptResponse = gptResult;
    return this;
  }
}
