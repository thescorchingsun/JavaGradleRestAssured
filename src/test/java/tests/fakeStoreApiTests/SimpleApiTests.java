package tests.fakeStoreApiTests;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

import models.fakeStoreApiUserModels.Address;
import models.fakeStoreApiUserModels.Geolocation;
import models.fakeStoreApiUserModels.Name;
import models.fakeStoreApiUserModels.UserRoot;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;


public class SimpleApiTests {

    //Тесты без рефакторинга

    @Test
    @DisplayName("Получение всех пользоваетлей. Статус код 200")
    public void getAllUsersTest() {
        given().get("https://fakestoreapi.com/users")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("Проверки id пользователя, паттерн zipcode, статус код 200")
    public void getSingleUserById() {
        int userId = 2;
        given().pathParam("userId",userId)
                .get("https://fakestoreapi.com/users/{userId}")
                .then().log().all()
                .body("id",equalTo(userId))
                .body("address.zipcode",matchesPattern("\\d{5}-\\d{4}")) //"zipcode": "12926-3874"
                .statusCode(200);
    }

    @Test
    @DisplayName("Получение определенного кол-ва пользователей, статус код 200")
    public void getAllUsersWithLimitTest() {
        int limitSize = 3;
        given().queryParam("limit", limitSize)
                .get("https://fakestoreapi.com/users")
                .then().log().all()
                .statusCode(200)
                .body("", hasSize(limitSize)); //hasSize(greaterThan(5)),hasSize(greaterThanOrEqualTo(3))
    }

    @Test //Сравнить два результата: отсортированный и обычный результат. Достать один признак, отвечающий за сортировку - id.
    @DisplayName("Сортировка id по убыванию")
    public void getAllUsersSortByDescTest() {
        //запрос с сортировкой
          String sortType = "desc";
          Response sortedResponse = given().queryParam("sort", sortType) //Response извлекаем из тела ответа
                  .get("https://fakestoreapi.com/users")
                  .then()
                  .log().all()
                  .extract().response();

        //запрос без сортировки
       Response notSortedResponse = given()
                .get("https://fakestoreapi.com/users")
                .then().log().all()
                .extract().response();

       //получение id с числами из отсортированного списка
        List<Integer> sortedResponseIds = sortedResponse.jsonPath().getList("id");
        //получение id с числами из неотсортированного списка
        List<Integer> notSortedResponseIds = notSortedResponse.jsonPath().getList("id");

        //отсортировать список notSortedResponseIds
        List<Integer> sortedByCode = notSortedResponseIds
                .stream()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList()); //собрать в список

        //Проверка, что значения разные
        Assertions.assertNotEquals(sortedResponseIds,notSortedResponseIds);
        //Проверка, что значения одинаковые
        Assertions.assertEquals(sortedByCode,sortedResponseIds);
    }


    //пример ответа в JSON
    /*
    {
  "address": {
    "geolocation": {
      "lat": "-37.3159",
      "long": "81.1496"
    },
    "city": "kilcoole",
    "street": "new road",
    "number": 7682,
    "zipcode": "12926-3874"
  },
  "id": 1,
  "email": "john@gmail.com",
  "username": "johnd",
  "password": "m38rmF$",
  "name": {
    "firstname": "john",
    "lastname": "doe"
  },
  "phone": "1-570-236-7033",
  "__v": 0
}
     */

    //сначала собираются маленькие части (name, geolocation), потом помещаются в более крупные части (address, bodyRequest)
    //__v  и id при запросе не нужны, они в ответе будут
    @Test
    @DisplayName("Добавление нового пользователя, статус код 200, id не 0")
    public void addNewUserTest() {
        Name name = new Name("Thomas","Anderson");
        Geolocation geolocation = new Geolocation("-37.3159","81.1496");

        Address address = Address.builder()
                .city("Moscow")
                .number(100)
                .zipcode("54321-4321")
                .street("Arbat 12")
                .geolocation(geolocation).build();

        UserRoot bodyRequest = UserRoot.builder()
                .name(name)
                .phone("79125689789")
                .email("fakeshop@mail.ru")
                .username("thomasadmin")
                .password("reR*4545")
                .address(address).build();

        given().body(bodyRequest)
                .post("https://fakestoreapi.com/users")
                .then().log().all()
                .statusCode(200)
                .body("id", notNullValue());
    }

    private UserRoot getTestUser() {
        Name name = new Name("Thomas", "Anderson");
        Geolocation geolocation = new Geolocation("-31.3123", "81.1231");

        Address address = Address.builder()
                .city("Moscow")
                .number(100)
                .zipcode("54231-4231")
                .street("Noviy Arbat 12")
                .geolocation(geolocation).build();

        return UserRoot.builder()
                .name(name)
                .phone("791237192")
                .email("fakemail@gmail.com")
                .username("thomasadmin")
                .password("mycoolpassword")
                .address(address).build();
    }

    @Test
    @DisplayName("Обновление пароля у пользователя")
    public void updateUserTest() {
        //получение пользователя и его текущего пароля
        UserRoot user = getTestUser();
        String oldPassword = user.getPassword();

        user.setPassword("newpass5445");
        given().body(user) //добавляется id пользователя
                .put("https://fakestoreapi.com/users/7") //"https://fakestoreapi.com/users/" + user.getId()
                .then().log().all()
                .body("password", not(equalTo(oldPassword)));
    }

    @Test
    @DisplayName("Удаление пользователя")
    public void deleteUserTest() {
        given().delete("https://fakestoreapi.com/users/6")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    public void authUserTest() {
        Map<String, String> userAuth = new HashMap<>();
        userAuth.put("username", "jimmie_k");
        userAuth.put("password", "klein*#%*");

        given().contentType(ContentType.JSON)
                .body(userAuth)
                .post("https://fakestoreapi.com/auth/login")
                .then().log().all()
                .statusCode(200)
                .body("token",notNullValue());
    }


}
