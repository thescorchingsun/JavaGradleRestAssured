package assertions;

import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class AssertableResponse {
    private final ValidatableResponse response;

    public AssertableResponse should(Condition condition) {
        condition.check(response);
        return this;
    }

    public String asJwt(){
        return response.extract().jsonPath().getString("token");
    }

    //параметризированный метод
    public <T> T as(Class<T> tClass) {
        return response.extract().as(tClass);
    }

    //для json path
    public <T> T as(String jsonPath, Class<T> tClass) {
        return response.extract().jsonPath().getObject(jsonPath, tClass);
    }

    //метод для извлечения в список
    public <T> List<T> asList(Class<T> tClass) {
       return response.extract().jsonPath().getList("",tClass);
    }

    //для указания конкретного пути
    public <T> List<T> asList(String jsonPath, Class<T> tClass) {
        return response.extract().jsonPath().getList(jsonPath,tClass);
    }

    //извлечение сырого ответа для вызова стандартных проверок
    public Response asResponse() {
        return response.extract().response();
    }



}
