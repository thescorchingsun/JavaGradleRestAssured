package tests.swaggerTests;

import io.restassured.common.mapper.TypeRef;
import models.swaggerModels.FullUser;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import utils.CustomTpl;
import models.swaggerModels.Info;
import models.swaggerModels.JwtAuthData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static io.restassured.RestAssured.given;

public class UserTestsWithoutRefactoring {
    private static Random random;

    @BeforeAll
    public static void setUp() {
        RestAssured.baseURI = "http://85.192.34.140:8080/api";
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter(),
                CustomTpl.customLogFilter().withCustomTemplates());
        random = new Random();
    }

    @Test
    @DisplayName("Успешная регистрация пользователя")
    public void positiveRegisterTest() {
        int randomNumber = Math.abs(random.nextInt());
        FullUser user = FullUser.builder()
                .login("Bobrova" + randomNumber)
                .pass("passwordCOOL")
                .build();

        Info info = given().contentType(ContentType.JSON)
                .body(user)
                .post("/signup")
                .then()
                .statusCode(201)
                .extract().jsonPath().getObject("info", Info.class);

        Assertions.assertEquals("User created", info.getMessage());
    }

    @Test
    @DisplayName("Регистрация с существующим логином")
    public void negativeRegisterLoginExistTest() {
        int randomNumber = Math.abs(random.nextInt());
        FullUser user = FullUser.builder()
                .login("Bobrova" + randomNumber)
                .pass("passwordCOOL")
                .build();

        Info info = given().contentType(ContentType.JSON)
                .body(user)
                .post("/signup")
                .then()
                .statusCode(201)
                .extract().jsonPath().getObject("info", Info.class);

        Assertions.assertEquals("User created", info.getMessage());

        Info errorInfo = given().contentType(ContentType.JSON)
                .body(user)
                .post("/signup")
                .then()
                .statusCode(400)
                .extract().jsonPath().getObject("info", Info.class);
        Assertions.assertEquals("Login already exist", errorInfo.getMessage());
    }

    @Test
    @DisplayName("Регистрация без пароля")
    public void negativeRegisterWithoutPasswordTest() {
        int randomNumber = Math.abs(random.nextInt());
        FullUser user = FullUser.builder()
                .login("Bobrova" + randomNumber)
                .build();

        Info info = given().contentType(ContentType.JSON)
                .body(user)
                .post("/signup")
                .then()
                .statusCode(400)
                .extract().jsonPath().getObject("info", Info.class);

        Assertions.assertEquals("Missing login or password", info.getMessage());
    }

    @Test
    @DisplayName("Успешная авторизация с админской УЗ")
    public void positiveAdminAuthTest() {
        JwtAuthData authData = new JwtAuthData("admin", "admin");

        String token = given().contentType(ContentType.JSON)
                .body(authData)
                .post("/login")
                .then()
                .statusCode(200)
                .extract().jsonPath().getString("token");

        Assertions.assertNotNull(token);
    }

    @Test
    @DisplayName("Успешная авторизация нового пользователя")
    public void positiveWithNewAuthUserTest() {
        int randomNumber = Math.abs(random.nextInt());
        FullUser user = FullUser.builder()
                .login("Bobrova" + randomNumber)
                .pass("passwordCOOL")
                .build();

        Info info = given().contentType(ContentType.JSON)
                .body(user)
                .post("/signup")
                .then()
                .statusCode(201)
                .extract().jsonPath().getObject("info", Info.class);

        Assertions.assertEquals("User created", info.getMessage());

        JwtAuthData authData = new JwtAuthData(user.getLogin(), user.getPass());

        String token = given().contentType(ContentType.JSON)
                .body(authData)
                .post("/login")
                .then()
                .statusCode(200)
                .extract().jsonPath().getString("token");

        Assertions.assertNotNull(token);
    }

    @Test
    @DisplayName("Авторизация с неправильным логином и паролем")
    public void negativeAuthTest() {
        JwtAuthData authData = new JwtAuthData("sdfsd54df", "rty444");

        given().contentType(ContentType.JSON)
                .body(authData)
                .post("/login")
                .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("Получение информации о пользователе")
    public void positiveGetUserInfoTest() {
        JwtAuthData authData = new JwtAuthData("admin", "admin");

        String token = given().contentType(ContentType.JSON)
                .body(authData)
                .post("/login")
                .then()
                .statusCode(200)
                .extract().jsonPath().getString("token");

        Assertions.assertNotNull(token);

        given().auth().oauth2(token) //given().header("Authorization","Bearer " + token)
                .get("/user")
                .then().statusCode(200);
    }

    @Test
    @DisplayName("Полчение информации о пользователе с невалидным токеном")
    public void negativeGetUserInfoInvalidJwtToknTest() {

        given().auth().oauth2("some values")
                .get("/user")
                .then().statusCode(401);
    }

    @Test
    @DisplayName("Полчение информации о пользователе без токена")
    public void negativeGetUserInfoWithoutJwtTokenTest() {
        given()
                .get("/user")
                .then().statusCode(401);
    }

    @Test
    @DisplayName("Обновление пароля у пользователя")
    public void positiveChangePasswordTest() {
        int randomNumber = Math.abs(random.nextInt()); //1. Регистрация пользователя
        FullUser user = FullUser.builder()
                .login("Bobrova" + randomNumber)
                .pass("passwordCOOL")
                .build();

        Info info = given().contentType(ContentType.JSON)
                .body(user)
                .post("/signup")
                .then().statusCode(201)
                .extract().jsonPath().getObject("info", Info.class);
        Assertions.assertEquals("User created", info.getMessage());

        JwtAuthData authData = new JwtAuthData(user.getLogin(), user.getPass()); //2.Авторизация нового пользователя

        String token = given().contentType(ContentType.JSON)
                .body(authData)
                .post("/login")
                .then().statusCode(200)
                .extract().jsonPath().getString("token");

        Map<String, String> password = new HashMap<>(); //3. смена пароля с подстановкой авторизационного токена
        String updatedPassValue = "newpassUpdated";
        password.put("password", updatedPassValue);

        Info updatePassInfo = given().contentType(ContentType.JSON)
                .auth().oauth2(token)
                .body(password)
                .put("/user")
                .then().extract().jsonPath().getObject("info", Info.class);
        Assertions.assertEquals("User password successfully changed", updatePassInfo.getMessage());

        authData.setPassword(updatedPassValue);  //4.Авторизация пользователя с новым паролем
        token = given().contentType(ContentType.JSON)
                .body(authData)
                .post("/login")
                .then().statusCode(200)
                .extract().jsonPath().getString("token");

        FullUser updatedUser = given().auth().oauth2(token) //5.получение информации о пользователе
                .get("/user")
                .then().statusCode(200)
                .extract().as(FullUser.class);
        Assertions.assertNotEquals(user.getPass(), updatedUser.getPass());
    }

    @Test
    @DisplayName("Смена паролля у админской УЗ")
    public void negativeChangeAdminPasswordTest() {

        JwtAuthData authData = new JwtAuthData("admin", "admin"); //2. Авторизация

        String token = given().contentType(ContentType.JSON)
                .body(authData)
                .post("/login")
                .then()
                .statusCode(200)
                .extract().jsonPath().getString("token");

        Map<String, String> password = new HashMap<>(); //3. Смена пароля с подстановкой авторизационного токена
        String updatedPassValue = "newpassUpdated";
        password.put("password", updatedPassValue);

        Info updatePassInfo = given().contentType(ContentType.JSON)
                .auth().oauth2(token)
                .body(password)
                .put("/user")
                .then()
                .statusCode(400)
                .extract().jsonPath().getObject("info", Info.class);
        Assertions.assertEquals("Cant update base users", updatePassInfo.getMessage());
    }

    @Test
    @DisplayName("Удаление админской УЗ")
    public void negativeDeleteAdminTest() {
        JwtAuthData authData = new JwtAuthData("admin", "admin"); //2. Авторизация

        String token = given().contentType(ContentType.JSON)
                .body(authData)
                .post("/login")
                .then()
                .statusCode(200)
                .extract().jsonPath().getString("token");

        Info info = given().auth().oauth2(token)
                .delete("/user")
                .then()
                .statusCode(400)
                .extract().jsonPath().getObject("info", Info.class);
        Assertions.assertEquals("Cant delete base users", info.getMessage());
    }

    @Test
    @DisplayName("Удаление УЗ пользователя")
    public void deleteNewUserTest() {
        int randomNumber = Math.abs(random.nextInt());
        FullUser user = FullUser.builder()
                .login("Bobrova" + randomNumber)
                .pass("passwordCOOL")
                .build();

        Info info = given().contentType(ContentType.JSON)
                .body(user)
                .post("/signup")
                .then()
                .statusCode(201)
                .extract().jsonPath().getObject("info", Info.class);

        Assertions.assertEquals("User created", info.getMessage());

        JwtAuthData authData = new JwtAuthData(user.getLogin(), user.getPass());

        String token = given().contentType(ContentType.JSON)
                .body(authData)
                .post("/login")
                .then()
                .statusCode(200)
                .extract().jsonPath().getString("token");

        Assertions.assertNotNull(token);

        Info infoDelete = given().auth().oauth2(token)
                .delete("/user")
                .then()
                .statusCode(200)
                .extract().jsonPath().getObject("info", Info.class);
        Assertions.assertEquals("User successfully deleted", infoDelete.getMessage());
    }

    @Test
    @DisplayName("Список пользователей равен или больше 3ех")
    public void positiveGetAllUsersTest() {
        List<String> users = given()
                .get("/users")
                .then()
                .statusCode(200)
                .extract().as(new TypeRef<List<String>>() {
                });
        Assertions.assertTrue(users.size() >= 3);
    }

}
