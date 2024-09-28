package tests.swaggerTests;

import models.swaggerModels.FullUser;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import listeners.CustomTpl;
import models.swaggerModels.Info;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static io.restassured.RestAssured.given;

public class

UserTests {
    private static Random random;

    @BeforeAll
    public static void setUp() {
        RestAssured.baseURI = "http://85.192.34.140:8080/";
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
                .post("/api/signup")
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
                .post("/api/signup")
                .then()
                .statusCode(201)
                .extract().jsonPath().getObject("info", Info.class);

        Assertions.assertEquals("User created", info.getMessage());

        Info errorInfo = given().contentType(ContentType.JSON)
                .body(user)
                .post("/api/signup")
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
                .post("/api/signup")
                .then()
                .statusCode(400)
                .extract().jsonPath().getObject("info", Info.class);

        Assertions.assertEquals("Missing login or password", info.getMessage());
    }


}
