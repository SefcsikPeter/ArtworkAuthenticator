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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
    String base64image = resizeImageForGPT4(artwork.image());
    String gptResult = imageAnalysisRequestToGPT4(base64image, artwork);

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
  private String resizeImageForGPT4(String base64Image) {
    String resizedBase64Image = "";
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

      // Convert image back into base64 format
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ImageIO.write(resizedImage, "jpg", baos);
      byte[] resizedBytes = baos.toByteArray();
      resizedBase64Image = Base64.getEncoder().encodeToString(resizedBytes);

      return resizedBase64Image;
    } catch (IOException e) {
      LOG.error("Error resizing image " + e.getMessage());
    }
    return resizedBase64Image;
  }

  private String imageAnalysisRequestToGPT4(String base64Image, ArtworkDetailDto artwork) {
    String gptResponse = "";
    try {
      String apiKey = "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";
      String jsonPayload = String.format("""
          {
            "model": "gpt-4-vision-preview",
            "messages": [
            {
                "role": "system",
                "content": "You are an artwork expert that is specialised in the works of Alfons Walde but can also help with other artists. Your task is to help the user determine the authenticity of an artwork by analysing the data that the user sends to you together with the image of the artwork. Please also return a long precision value from 0 to 1 to describe the probability of the artwork being authentic. Please keep your answer concise and professional and make sure to include your authenticity rating in the following format: 'Authenticity Rating: [your rating]'. You don't need to mention the need of consultation with experts and the usage of additional data and methods, that will be used by the user, your only goal is to give an estimate using the information given to you and all your capabilities."
              },
              {
                "role": "user",
                "content": [
                  {
                    "type": "text",
                    "text": "Title: %s, Artist: %s, Selling Gallery: %s, Price: %s, Description or/and additional information: %s"
                  },
                  {
                    "type": "image_url",
                    "image_url": {
                      "url": "data:image/jpeg;base64,%s",
                      "detail": "low"
                    }
                  }
                ]
              }
            ],
            "max_tokens": 600
          }""", artwork.title(), artwork.artist(), artwork.gallery(), artwork.price(), artwork.description(), base64Image);

      HttpClient client = HttpClient.newHttpClient();
      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create("https://api.openai.com/v1/chat/completions"))
          .header("Content-Type", "application/json")
          .header("Authorization", "Bearer " + apiKey)
          .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
          .build();

      // Send the request and get the response
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      // Store the response body in a variable
      String responseBody = response.body();

      ObjectMapper objectMapper = new ObjectMapper();
      JsonNode jsonResponse = objectMapper.readTree(responseBody);

      if (jsonResponse.has("choices") && jsonResponse.get("choices").isArray()) {
        JsonNode choices = jsonResponse.get("choices");
        if (choices.size() > 0) {
          JsonNode firstChoice = choices.get(0);
          if (firstChoice.has("message") && firstChoice.get("message").has("content")) {
            gptResponse = firstChoice.get("message").get("content").asText();
          }
        }
      } else {
        System.err.println("No choices available in the response.");
        System.err.println(responseBody);
      }
    } catch (IOException | InterruptedException e) {
      LOG.error("could not get response from gpt-4 vision " + e.getMessage());
    }
    return gptResponse;
  }
}
