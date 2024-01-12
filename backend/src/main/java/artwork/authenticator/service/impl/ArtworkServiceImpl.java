package artwork.authenticator.service.impl;

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
import artwork.authenticator.type.Artist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
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
  public Long analyse(ArtworkDetailDto artwork) {
    LOG.trace("analyse({})", artwork);
    // Save artwork
    Artwork analysedArtwork = artworkDao.create(artwork);

    String baseFolderPath = "C:/Users/ptsef/OneDrive/Desktop/BSC/UserInterface/template-java/backend/images";

    Long artworkId = analysedArtwork.getId();
    String neuralNetResult = runPythonScript(artwork.image(), artworkId, Artist.getArtistIndex(artwork.artist()), baseFolderPath);
    // TODO: Add gpt4 api access
    String imagePath = this.resizeImageForGPT4(artwork.image(), baseFolderPath, artworkId);
    String gptResult = "this is a test for the gpt result";

    ArtworkResultDto resultDto = new ArtworkResultDto(artworkId, neuralNetResult, gptResult);
    ArtworkResult result = artworkResultDao.create(resultDto);

    return result.getId();
  }

  @Override
  public ArtworkDetailDto getById(Long id) throws NotFoundException {
    LOG.trace("getById({})", id);
    Artwork artwork = artworkDao.getById(id);
    return artworkMapper.entityToDto(artwork);
  }

  private String runPythonScript(String image, Long artworkId, int artistIndex, String baseFolderPath) {
    String output = "";
    //TODO: change these paths if on different computer or trying to execute with different env
    String pythonScriptPath = "C:\\Users\\ptsef\\OneDrive\\Desktop\\BSC\\UserInterface\\template-java\\backend\\src\\main\\java\\artwork\\authenticator\\python\\authenticator.py";
    String condaEnvPath = "C:\\ProgramData\\miniconda3\\envs\\gcv_exercise_4";

    String condaActivateScript = "conda activate " + condaEnvPath;
    String pythonExecutable = condaEnvPath + "\\python";

    try {
      String imagePath = resizeImage(image, baseFolderPath, artworkId);
      ProcessBuilder pb =
          new ProcessBuilder("cmd", "/c", condaActivateScript, "&&", pythonExecutable, pythonScriptPath, imagePath, "" + artistIndex);
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

  private String resizeImage(String base64Image, String baseFolderPath, Long id) {
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

      // Save resized image to the specified file path
      String outputPath = baseFolderPath + "/img" + id + ".jpeg";
      File outputFile = new File(outputPath);
      ImageIO.write(resizedImage, "jpg", outputFile);

      return outputPath;
    } catch (IOException e) {
      LOG.error("Error resizing image " + e.getMessage());
    }
    return null;
  }

  private String resizeImageForGPT4(String base64Image, String baseFolderPath, Long id) {
    String image = "";
    try {
      base64Image = base64Image.substring(base64Image.indexOf(",") + 1);
      byte[] imageBytes = Base64.getDecoder().decode(base64Image);
      ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
      BufferedImage originalImage = ImageIO.read(bais);

      int width = originalImage.getWidth();
      int height = originalImage.getHeight();
      int maxPx = Math.max(width, height);

      int newWidth;
      int newHeight;
      if (maxPx > 512) {
        if (maxPx == width) {
          newWidth = 512;
          newHeight = (int)(height * (512./width));
        } else {
          newHeight = 512;
          newWidth = (int)(width * (512./height));
        }
      } else {
        newWidth = width;
        newHeight = height;
      }

      // Resize Image
      BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
      Graphics2D g = resizedImage.createGraphics();
      g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
      g.dispose();

      // Save resized image to the specified file path
      String outputPath = baseFolderPath + "/gptimg" + id + ".jpeg";
      File outputFile = new File(outputPath);
      ImageIO.write(resizedImage, "jpg", outputFile);

      return outputPath;
    } catch (IOException e) {
      LOG.error("Error resizing image " + e.getMessage());
    }
    return image;
  }
}
