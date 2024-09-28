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
public class UserRoot {

    @JsonProperty("password")
    private String password;

    @JsonProperty("address")
    private Address address;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("__v")
    private int v;

    @JsonProperty("name")
    private Name name;

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("email")
    private String email;

    @JsonProperty("username")
    private String username;

}
