package artwork.authenticator.entity;

public class ArtworkResult {
  private Long id;
  private Long artworkId;
  private String neuralNetResult;
  private String gptResult;

  public Long getId() {
    return id;
  }

  public ArtworkResult setId(Long id) {
    this.id = id;
    return this;
  }

  public Long getArtworkId() {
    return artworkId;
  }

  public ArtworkResult setArtworkId(Long artworkId) {
    this.artworkId = artworkId;
    return this;
  }

  public String getNeuralNetResult() {
    return neuralNetResult;
  }

  public ArtworkResult setNeuralNetResult(String neuralNetResult) {
    this.neuralNetResult = neuralNetResult;
    return this;
  }

  public String getGptResult() {
    return gptResult;
  }

  public ArtworkResult setGptResult(String gptResult) {
    this.gptResult = gptResult;
    return this;
  }
}
