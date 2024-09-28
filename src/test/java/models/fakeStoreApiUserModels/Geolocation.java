package models.fakeStoreApiUserModels;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
//JSON https://fakestoreapi.com/users/1
public class Geolocation {

        @JsonProperty("lat")
        private String lat;

        @JsonProperty("long")
        private String jsonMemberLong;

}
