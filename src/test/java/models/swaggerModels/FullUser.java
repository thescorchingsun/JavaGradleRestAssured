package models.swaggerModels;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)//игнорировать незаполненное значение
@JsonIgnoreProperties(ignoreUnknown = true)
public class FullUser {

    @JsonProperty("pass")
    private String pass;

    @JsonProperty("games")
    private List<GamesItem> games;

    @JsonProperty("login")
    private String login;
}

