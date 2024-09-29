package tests.fakeStoreApiTests;

import io.qameta.allure.Issue;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import utils.CustomTpl;
import models.fakeStoreApiUserModels.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;


public class SimpleApiTestsWithRefactoring {

    @BeforeAll
    public static void setUp() {
        RestAssured.baseURI = "https://fakestoreapi.com";
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter(),
                CustomTpl.customLogFilter().withCustomTemplates()); //фильтры для логирования запроса и ответа + Allure
    }

    @Test
    @DisplayName("Получение всех пользоваетлей. Статус код 200")
    @Issue("JIRA-10")
    public void getAllUsersTest() {
        given().get("/users")
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("Проверки id пользователя, паттерн zipcode, статус код 200")
    @Issue("JIRA-11")
    public void getSingleUserById() {
        int userId = 2;
        UserRoot response = given()
                .pathParam("userId", userId)
                .get("/users/{userId}")
                .then()
                .statusCode(200)
                .extract().as(UserRoot.class);
 /*
        //Пример. Как извлечь один параметр из json
        Name name = given()
                .pathParam("userId",userId)
                .get("/users/{userId}")
                .then()
                .extract().jsonPath().getObject("name", Name.class);
*/
        Assertions.assertEquals(userId, response.getId());
        Assertions.assertTrue(response.getAddress().getZipcode().matches("\\d{5}-\\d{4}"));
    }

    @ParameterizedTest()
    @ValueSource(ints = {1,10,20})
    @DisplayName("Получение определенного кол-ва пользователей, статус код 200")
    public void getAllUsersWithLimitTest(int limitSize) {
        List<UserRoot> users = given().queryParam("limit", limitSize)
                .get("/users")
                .then()
                .statusCode(200)
                .extract()//ответ извлечь в список класса root
                .jsonPath().getList("", UserRoot.class);
        Assertions.assertEquals(limitSize, users.size());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 40})
    @DisplayName("Получение определенного кол-ва пользователей, статус код 200")
    public void getAllUsersWithLimitErrorParams(int limitSize){
        List<UserRoot> users = given()
                .queryParam("limit", limitSize)
                .get("/users")
                .then()
                .statusCode(200)
                .extract().as(new TypeRef<List<UserRoot>>() {});
        Assertions.assertNotEquals(limitSize, users.size());
    }

    @Test
    @DisplayName("Сортировка id по убыванию")
    public void getAllUsersSortByDescTest() {
        //запрос с сортировкой
        String sortType = "desc";
        List<UserRoot> usersSorted = given()
                .queryParam("sort", sortType) //Response извлекаем из тела ответа
                .get("/users")
                .then()
                .extract().as(new TypeRef<List<UserRoot>>() {
                });

        List<UserRoot> usersNotSorted = given()
                .get("/users")
                .then()
                .extract().as(new TypeRef<List<UserRoot>>() {
                });

        List<Integer> sortedResponseIds = usersSorted
                .stream()
                .map(UserRoot::getId).collect(Collectors.toList());

        List<Integer> sortedByCode = usersNotSorted.stream()
                .map(UserRoot::getId)
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());

        Assertions.assertNotEquals(usersSorted, usersNotSorted);
        Assertions.assertEquals(sortedResponseIds, sortedByCode);
    }


    private UserRoot getTestUser() {
        Random random = new Random();
        Name name = new Name("Thomas", "Anderson");
        Geolocation geolocation = new Geolocation("-31.3123", "81.1231");

        Address address = Address.builder()
                .city("Moscow")
                .number(random.nextInt(100))
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
    @DisplayName("Добавление нового пользователя, статус код 200, id не 0")
    public void addNewUserTest() {

        UserRoot user = getTestUser();

        Integer userId = given().body(user)
                .post("/users")
                .then()
                .statusCode(200)
                .extract().jsonPath().getInt("id");

        Assertions.assertNotNull(userId);
    }


    @Test
    @DisplayName("Обновление пароля у пользователя")
    public void updateUserTest() {
        UserRoot user = getTestUser();
        String oldPassword = user.getPassword();

        Integer userId = given().body(user)
                .post("/users")
                .then()
                .statusCode(200)
                .extract().jsonPath().getInt("id");

        user.setPassword("newpass111");

        UserRoot updatedUser = given()
                .body(user)
                .pathParam("userId", userId)
                .put("/users/{userId}")
                .then().extract().as(UserRoot.class);

        Assertions.assertNotEquals(updatedUser.getPassword(), oldPassword);
    }

    @Test
    @DisplayName("Удаление пользователя")
    public void deleteUserTest() {
        given().delete("/users/6")
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("Получение авторизационного токена")
    public void authUserTest() {
        AuthData authData = new AuthData("jimmie_k","klein*#%*");

       String token = given().contentType(ContentType.JSON)
                .body(authData)
                .post("/auth/login")
                .then()
                .statusCode(200)
               .extract().jsonPath().getString("token");
       Assertions.assertNotNull(token);

    }


}
