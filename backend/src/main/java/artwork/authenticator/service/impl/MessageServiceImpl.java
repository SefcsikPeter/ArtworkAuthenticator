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
import artwork.authenticator.service.MessageService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
  public GPTResponseDto create(UserMessageDto message) throws NotFoundException {
    LOG.trace("create({})", message);

    ArtworkResult result = resultDao.getById(message.resultId());
    Artwork artwork = artworkDao.getById(result.getArtworkId());
    List<Message> messages = dao.getAllByResultId(message.resultId());

    String gptResponse = feedbackRequestToGPT4(artwork, result, messages, message.userMessage());

    dao.create(new MessageDto(message.resultId(), message.userMessage(), gptResponse));
    return new GPTResponseDto(gptResponse);
  }

  private String feedbackRequestToGPT4(Artwork artwork, ArtworkResult result, List<Message> messages, String userMessage) {
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
          artwork.getImage(),
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
    return gptResponse.replace('"', '\'');
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
}
