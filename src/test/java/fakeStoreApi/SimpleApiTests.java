package fakeStoreApi;

import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;


public class SimpleApiTests {

    @Test
    @DisplayName("Получение всех пользоваетлей. Статус код 200")
    public void getAllUsersTest() {
        given().get("https://fakestoreapi.com/users")
                .then()
                .log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("Проверки id пользователя, паттерн zipcode, статус код 200")
    public void getSingleUserById() {
        int userId = 2;
        given().pathParam("userId",userId)
                .get("https://fakestoreapi.com/users/{userId}")
                .then().
                log().all()
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
                .then()
                .log().all()
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
                .then()
                .log().all()
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
        Assertions.assertEquals(sortedResponseIds,notSortedResponseIds);
        //Проверка, что значения одинаковые
        Assertions.assertEquals(sortedByCode,sortedResponseIds);

    }




}
