package artwork.authenticator.service.impl;

import artwork.authenticator.dto.GPTResponseDto;
import artwork.authenticator.dto.MessageDto;
import artwork.authenticator.dto.MessageListDto;
import artwork.authenticator.dto.UserMessageDto;
import artwork.authenticator.entity.Artwork;
import artwork.authenticator.entity.ArtworkResult;
import artwork.authenticator.entity.Message;
import artwork.authenticator.exception.NotFoundException;
import artwork.authenticator.mapper.MessageMapper;
import artwork.authenticator.persistence.ArtworkDao;
import artwork.authenticator.persistence.ArtworkResultDao;
import artwork.authenticator.persistence.MessageDao;
import artwork.authenticator.rest.ApiKeyEndpoint;
import artwork.authenticator.service.MessageService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

@Service
public class MessageServiceImpl implements MessageService {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final MessageDao dao;
  private final ArtworkResultDao resultDao;
  private final ArtworkDao artworkDao;
  private final MessageMapper mapper;
  public MessageServiceImpl(MessageDao dao, ArtworkResultDao resultDao, ArtworkDao artworkDao, MessageMapper mapper) {
    this.dao = dao;
    this.resultDao = resultDao;
    this.artworkDao = artworkDao;
    this.mapper = mapper;
  }
  @Override
  public Stream<MessageListDto> getAllByResultId(Long resultId) {
    LOG.trace("getAllByResultId({})", resultId);

    List<Message> messages = dao.getAllByResultId(resultId);
    return messages.stream().map(mapper::entityToListDto);
  }

  @Override
  public GPTResponseDto create(UserMessageDto message) throws NotFoundException, IOException {
    LOG.trace("create({})", message);

    ArtworkResult result = resultDao.getById(message.resultId());
    Artwork artwork = artworkDao.getById(result.getArtworkId());
    List<Message> messages = dao.getAllByResultId(message.resultId());

    String base64Image = imageFromEncodedPathToBase64(artwork.getImage());
    String imageType = getImageType(artwork.getImage());
    if (imageType.equals("png")) {
      base64Image = "data:image/png;base64," + base64Image;
    } else if (imageType.equals("JPEG")) {
      base64Image = "data:image/jpeg;base64," + base64Image;
    }

    String gptResponse = feedbackRequestToGPT4(artwork, result, messages, message.userMessage(), base64Image);

    dao.create(new MessageDto(message.resultId(), message.userMessage(), gptResponse));
    return new GPTResponseDto(gptResponse);
  }

  private String feedbackRequestToGPT4(Artwork artwork, ArtworkResult result, List<Message> messages, String userMessage, String image) {
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
              },
              {
              "role": "assistant",
              "content": [
                {
                "type": "text",
                "text": "%s"
              }
              ]
              },
              """,
          artwork.getTitle(),
          artwork.getArtist(),
          artwork.getGallery(),
          artwork.getPrice(),
          artwork.getDescription(),
          image, // TODO: change to loading image from path
          result.getGptResult().replace('\n', ' '));
      if (!messages.isEmpty()) {
        for (Message message : messages) {
          jsonPayload = appendUserMessageAndGptResponse(jsonPayload, message);
        }
      }
      jsonPayload = appendNewMessage(jsonPayload, userMessage);
      jsonPayload = appendJsonEnding(jsonPayload);

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

  private String appendJsonEnding(String jsonPayload) {
    jsonPayload += "  ],\n";
    jsonPayload += "  \"max_tokens\": 600\n";
    jsonPayload += "}\n";
    return jsonPayload;
  }

  private String appendUserMessageAndGptResponse(String jsonPayload, Message message) {
    jsonPayload += "    {\n";
    jsonPayload += "      \"role\": \"user\",\n";
    jsonPayload += "      \"content\": [\n";
    jsonPayload += "      {";
    jsonPayload += "        \"type\": \"text\",\n";
    jsonPayload += "        \"text\": \"" + message.getUserMessage().replace('\n', ' ') + "\"\n";
    jsonPayload += "      }";
    jsonPayload += "    ]\n";
    jsonPayload += "    },\n";

    jsonPayload += "    {\n";
    jsonPayload += "      \"role\": \"assistant\",\n";
    jsonPayload += "      \"content\": [\n";
    jsonPayload += "      {";
    jsonPayload += "        \"type\": \"text\",\n";
    jsonPayload += "        \"text\": \"" + message.getGptResponse().replace('\n', ' ') + "\"\n";
    jsonPayload += "       }";
    jsonPayload += "    ]\n";
    jsonPayload += "    },\n";

    return jsonPayload;
  }

  private String appendNewMessage(String jsonPayload, String userMessage) {
    jsonPayload += "  {\n";
    jsonPayload += "  \"role\": \"user\",\n";
    jsonPayload += "  \"content\": [\n";
    jsonPayload += "    {\n";
    jsonPayload += "      \"type\": \"text\",\n";
    jsonPayload += "      \"text\": \"" + userMessage.replace('\n', ' ') + "\"\n";
    jsonPayload += "    }\n";
    jsonPayload += "  ]\n";
    jsonPayload += "  }\n";
    return jsonPayload;
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

  private String getImageType(String filePath) {
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
