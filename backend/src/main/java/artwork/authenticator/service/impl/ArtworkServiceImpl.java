package artwork.authenticator.service.impl;

import artwork.authenticator.dto.ArtworkDetailAndResultDto;
import artwork.authenticator.dto.ArtworkDetailDto;
import artwork.authenticator.dto.ArtworkResultDto;
import artwork.authenticator.entity.Artwork;
import artwork.authenticator.entity.ArtworkResult;
import artwork.authenticator.exception.NotFoundException;
import artwork.authenticator.mapper.ArtworkMapper;
import artwork.authenticator.mapper.ArtworkResultMapper;
import artwork.authenticator.persistence.ArtworkDao;
import artwork.authenticator.persistence.ArtworkResultDao;
import artwork.authenticator.service.ArtworkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.util.Base64;

@Service
public class ArtworkServiceImpl implements ArtworkService {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final ArtworkDao artworkDao;
  private final ArtworkResultDao artworkResultDao;
  private final ArtworkResultMapper artworkResultMapper;
  private final ArtworkMapper artworkMapper;

  public ArtworkServiceImpl(
      ArtworkDao artworkDao,
      ArtworkResultDao artworkResultDao,
      ArtworkResultMapper artworkResultMapper,
      ArtworkMapper artworkMapper) {
    this.artworkDao = artworkDao;
    this.artworkResultDao = artworkResultDao;
    this.artworkResultMapper = artworkResultMapper;
    this.artworkMapper = artworkMapper;
  }

  @Override
  public ArtworkDetailAndResultDto analyse(ArtworkDetailDto artwork) {
    LOG.trace("analyse({})", artwork);
    // Save artwork
    Artwork analysedArtwork = artworkDao.create(artwork);

    // TODO: do the image analysis
    Long artworkId = analysedArtwork.getId();
    String neuralNetResult = runPythonScript(artwork.image());
    String gptResult = "this is a test for the gpt result";

    ArtworkResultDto resultDto = new ArtworkResultDto(artworkId, neuralNetResult, gptResult);
    ArtworkResult result = artworkResultDao.create(resultDto);

    return artworkResultMapper.entityToDto(analysedArtwork, result);
  }

  @Override
  public ArtworkDetailDto getById(Long id) throws NotFoundException {
    LOG.trace("getById({})", id);
    Artwork artwork = artworkDao.getById(id);
    return artworkMapper.entityToDto(artwork);
  }

  private String runPythonScript(String image) {
    String output = "";
    try {
      String pythonScriptPath = "C:\\Users\\ptsef\\OneDrive\\Desktop\\BSC\\UserInterface\\template-java\\backend\\src\\main\\java\\artwork\\authenticator\\python\\authenticator.py";
      String condaEnvPath = "C:\\ProgramData\\miniconda3\\envs\\gcv_exercise_4";
      String condaActivateScript = "conda activate " + condaEnvPath;
      String pythonExecutable = condaEnvPath + "\\python";

      image = resizeImage(image);
      ProcessBuilder pb = new ProcessBuilder("cmd", "/c", condaActivateScript, "&&", pythonExecutable, pythonScriptPath, image);
      Process p = pb.start();

      BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
      BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

      String line;
      while ((line = in.readLine()) != null) {
        System.out.println(line);
        output = line;
      }

      while ((line = stdError.readLine()) != null) {
        System.err.println(line);
      }

      int exitCode = p.waitFor();
      System.out.println("Exited with code " + exitCode);
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
    return output;
  }

  public String resizeImage(String base64Image) {
    try {
      base64Image = base64Image.substring(base64Image.indexOf(",") + 1);

      // Decode Base64 to byte array
      byte[] imageBytes = Base64.getDecoder().decode(base64Image);

      // Convert byte array to BufferedImage
      ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
      BufferedImage originalImage = ImageIO.read(bais);

      // Resize Image
      int newWidth = 128; // desired width
      int newHeight = 128; // desired height
      BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
      Graphics2D g = resizedImage.createGraphics();
      g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
      g.dispose();

      // Encode resized image to Base64
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ImageIO.write(resizedImage, "jpg", baos);
      byte[] resizedBytes = baos.toByteArray();
      String img = Base64.getEncoder().encodeToString(resizedBytes);
      return img;
    } catch (IOException e) {
      LOG.error("Error resizing image " + e.getMessage());
    }
    return null;
  }
}
