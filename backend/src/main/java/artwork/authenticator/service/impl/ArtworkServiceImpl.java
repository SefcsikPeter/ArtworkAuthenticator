package artwork.authenticator.service.impl;

import artwork.authenticator.dto.ArtworkDetailAndResultDto;
import artwork.authenticator.dto.ArtworkDetailDto;
import artwork.authenticator.dto.ArtworkResultDto;
import artwork.authenticator.entity.Artwork;
import artwork.authenticator.entity.ArtworkResult;
import artwork.authenticator.mapper.ArtworkResultMapper;
import artwork.authenticator.persistence.ArtworkDao;
import artwork.authenticator.persistence.ArtworkResultDao;
import artwork.authenticator.service.ArtworkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;

@Service
public class ArtworkServiceImpl implements ArtworkService {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final ArtworkDao artworkDao;
  private final ArtworkResultDao artworkResultDao;
  private final ArtworkResultMapper mapper;

  public ArtworkServiceImpl(ArtworkDao artworkDao, ArtworkResultDao artworkResultDao, ArtworkResultMapper mapper) {
    this.artworkDao = artworkDao;
    this.artworkResultDao = artworkResultDao;
    this.mapper = mapper;
  }

  @Override
  public ArtworkDetailAndResultDto analyse(ArtworkDetailDto artwork) {
    LOG.trace("analyse({})", artwork);
    // Save artwork
    Artwork analysedArtwork = artworkDao.create(artwork);

    // TODO: do the image analysis
    Long artworkId = analysedArtwork.getId();
    String neuralNetResult = runPythonScript("LETSTESTTHIS");
    String gptResult = "this is a test for the gpt result";

    ArtworkResultDto resultDto = new ArtworkResultDto(artworkId, neuralNetResult, gptResult);
    ArtworkResult result = artworkResultDao.create(resultDto);

    return mapper.entityToDto(analysedArtwork, result);
  }

  private String runPythonScript(String image) {
    String output = "";
    try {
      String pythonScriptPath = "C:\\Users\\ptsef\\OneDrive\\Desktop\\BSC\\UserInterface\\template-java\\backend\\src\\main\\java\\artwork\\authenticator\\python\\authenticator.py";

      ProcessBuilder pb = new ProcessBuilder("python", pythonScriptPath, image);
      Process p = pb.start();

      BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
      String line;
      while ((line = in.readLine()) != null) {
        System.out.println(line);
        output = line;
      }

      int exitCode = p.waitFor();
      System.out.println("Exited with code " + exitCode);
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
    return output;
  }
}
