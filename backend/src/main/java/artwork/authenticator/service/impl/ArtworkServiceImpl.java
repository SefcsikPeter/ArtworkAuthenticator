package artwork.authenticator.service.impl;

import artwork.authenticator.dto.ArtworkDetailDto;
import artwork.authenticator.dto.ArtworkResultDto;
import artwork.authenticator.entity.Artwork;
import artwork.authenticator.entity.ArtworkResult;
import artwork.authenticator.exception.NotFoundException;
import artwork.authenticator.mapper.ArtworkMapper;
import artwork.authenticator.persistence.ArtworkDao;
import artwork.authenticator.persistence.ArtworkResultDao;
import artwork.authenticator.rest.ApiKeyEndpoint;
import artwork.authenticator.service.ArtworkService;
import artwork.authenticator.type.Artist;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ArtworkServiceImpl implements ArtworkService {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final ArtworkDao artworkDao;
  private final ArtworkResultDao artworkResultDao;
  private final ArtworkMapper artworkMapper;

  public ArtworkServiceImpl(
      ArtworkDao artworkDao,
      ArtworkResultDao artworkResultDao,
      ArtworkMapper artworkMapper) {
    this.artworkDao = artworkDao;
    this.artworkResultDao = artworkResultDao;
    this.artworkMapper = artworkMapper;
  }

  @Override
  public Long analyse(ArtworkDetailDto artwork) throws IOException {
    LOG.trace("analyse({})", artwork);
    Artwork analysedArtwork = artworkDao.create(artwork);
    String base64Image = imageFromEncodedPathToBase64(artwork.image());
    String imageType = getImageType(artwork.image());
    if (imageType.equals("png")) {
      base64Image = "data:image/png;base64," + base64Image;
    } else if (imageType.equals("JPEG")) {
      base64Image = "data:image/jpeg;base64," + base64Image;
    }
    Long artworkId = analysedArtwork.getId();
    //String neuralNetResult = runPythonScript(artwork.image(), Artist.getArtistIndex(artwork.artist()));
    String neuralNetResult = "";
    String gptResult = imageAnalysisRequestToGPT4(base64Image, artwork);

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

  @Override
  public Map<Long, ArtworkDetailDto> getAllById(Collection<Long> ids) throws NotFoundException {
    LOG.trace("getAllById({})", ids);
    Map<Long, ArtworkDetailDto> artworks =
        artworkDao.getAllById(ids).stream()
            .map(artworkMapper::entityToDto)
            .collect(Collectors.toUnmodifiableMap(ArtworkDetailDto::id, Function.identity()));
    for (final var id : ids) {
      if (!artworks.containsKey(id)) {
        throw new NotFoundException("Artwork with ID %d not found".formatted(id));
      }
    }
    return artworks;
  }

  private String runPythonScript(String image, int artistIndex) throws IOException{
    // TODO remove
    boolean testmode = false;
    String output = "";
    //TODO: change these paths if on different computer or trying to execute with different env
    String pythonScriptPath = "";
    String condaEnvPath = "";
    if (testmode) {
      pythonScriptPath = "C:\\Users\\ptsef\\OneDrive\\Desktop\\BSC\\UserInterface\\template-java\\backend\\src\\main\\java\\artwork\\authenticator\\python\\authenticator.py";
      condaEnvPath = "..\\conda-env\\auth-env";
    } else {
      String workingDirectory = System.getProperty("user.dir");
      pythonScriptPath = workingDirectory + "\\resources\\backend\\python\\authenticator.py";
      condaEnvPath = workingDirectory + "\\resources\\conda-env\\auth-env";
    }

    String condaActivateScript = "conda activate " + condaEnvPath;
    String pythonExecutable = condaEnvPath + "\\python";

    String imagePath = image.replaceFirst("^file://", "");
    imagePath = "\"" + imagePath + "\""; // Ensures the path is correctly quoted

    List<String> commands = new ArrayList<>();
    commands.add(pythonExecutable);
    commands.add(pythonScriptPath);
    commands.add(imagePath);
    commands.add(String.valueOf(artistIndex));

    try {
      ProcessBuilder pb = new ProcessBuilder(commands);
      Process p = pb.start();

      BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
      BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

      String line;
      while ((line = in.readLine()) != null) {
        System.out.println(line);
        output = line;
      }

      while ((line = stdError.readLine()) != null) {
        LOG.error(line);
      }

      int exitCode = p.waitFor();
      System.out.println("Exited with code " + exitCode);
    } catch (IOException | InterruptedException e) {
      throw new IOException("Running the python script failed: conda path: " + condaEnvPath + " script path: "
          + pythonScriptPath, e);
    }
    return output;
  }

  private String imageAnalysisRequestToGPT4(String base64Image, ArtworkDetailDto artwork) {
    String gptResponse = "";
    try {
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
                      "url": "%s",
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
          .header("Authorization", "Bearer " + ApiKeyEndpoint.getApiKey())
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
    return sanitizeForJSON(gptResponse);
  }

  private String imageFromEncodedPathToBase64(String imagePath) throws IOException {
    String urlDecodedPath = URLDecoder.decode(imagePath, StandardCharsets.UTF_8);
    byte[] decodedBytes = Base64.getDecoder().decode(urlDecodedPath);
    String decodedPath = new String(decodedBytes, StandardCharsets.UTF_8);
    File file = new File(decodedPath);
    byte[] fileContent = new byte[(int) file.length()];
    try (FileInputStream fileInputStream = new FileInputStream(file)) {
      fileInputStream.read(fileContent);
      return Base64.getEncoder().encodeToString(fileContent);
    } catch (IOException e) {
      throw new IOException("Could not convert image to base 64");
    }
  }

  private String getImageType(String filePath) throws IOException {
    String urlDecodedPath = URLDecoder.decode(filePath, StandardCharsets.UTF_8);
    byte[] decodedBytes = Base64.getDecoder().decode(urlDecodedPath);
    String decodedPath = new String(decodedBytes, StandardCharsets.UTF_8);
    File file = new File(decodedPath);
    try (ImageInputStream iis = ImageIO.createImageInputStream(file)) {
      Iterator<ImageReader> iter = ImageIO.getImageReaders(iis);
      if (!iter.hasNext()) {
        throw new IOException("No readers found for the given image.");
      }
      ImageReader reader = iter.next();
      return reader.getFormatName();
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  private String sanitizeForJSON(String input) {
    if (input == null) {
      return null;
    }

    StringBuilder sanitized = new StringBuilder();
    for (char c : input.toCharArray()) {
      switch (c) {
        case '\\':
          sanitized.append("\\\\");
          break;
        case '\"':
          sanitized.append("\\\"");
          break;
        case '\n':
          sanitized.append("\\n");
          break;
        case '\r':
          sanitized.append("\\r");
          break;
        case '\t':
          sanitized.append("\\t");
          break;
        case '\b':
          sanitized.append("\\b");
          break;
        case '\f':
          sanitized.append("\\f");
          break;
        default:
          // Handle Unicode control characters (U+0000 to U+001F)
          if (c >= 0x0000 && c <= 0x001F) {
            sanitized.append(String.format("\\u%04x", (int) c));
          } else {
            sanitized.append(c);
          }
          break;
      }
    }

    return sanitized.toString();
  }
}
