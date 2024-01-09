package artwork.authenticator.dto;

import artwork.authenticator.type.Artist;

public record ArtworkDetailAndResultDto (
  Long artworkId,
  Long resultId,
  String title,
  Artist artist,
  String gallery,
  String price,
  String description,
  String image,
  String neuralNetResult,
  String gptResult
) {}
