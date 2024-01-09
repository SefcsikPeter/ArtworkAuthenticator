package artwork.authenticator.entity;

public class ArtworkResult {
  private Long id;
  private Long artworkId;
  private String neuralNetResult;
  private String gptResult;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getArtworkId() {
    return artworkId;
  }

  public void setArtworkId(Long artworkId) {
    this.artworkId = artworkId;
  }

  public String getNeuralNetResult() {
    return neuralNetResult;
  }

  public void setNeuralNetResult(String neuralNetResult) {
    this.neuralNetResult = neuralNetResult;
  }

  public String getGptResult() {
    return gptResult;
  }

  public void setGptResult(String gptResult) {
    this.gptResult = gptResult;
  }
}
