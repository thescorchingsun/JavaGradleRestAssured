package tests.swaggerTests;

import assertions.Conditions;
import io.qameta.allure.Issue;
import io.restassured.RestAssured;

import io.restassured.response.Response;
import listeners.AdminUser;

import models.swaggerModels.FullUser;
import models.swaggerModels.Info;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.*;

import java.util.List;

import static assertions.Conditions.hasMessage;
import static assertions.Conditions.hasStatusCode;
import static utils.RandomTestData.*;


public class UserTestsWithRefactoring extends BaseApiTest {

    @Test
    @Issue("JIRA-105")
    @DisplayName("Успешная регистрация пользователя без игры")
    public void positiveRegisterWithoutGamesTest() {
        userService.register(randomUser)
                .should(hasStatusCode(201))
                .should(hasMessage("User created"));
    }

    @Test
    @DisplayName("Успешная регистрация пользователя c игрой") //библиотека Faker
    public void positiveRegisterWithGamesTest() {
        FullUser user = getRandomUserWithGames();
        userService.register(user)
                .should(hasStatusCode(201))
                .should(hasMessage("User created"));
    }

    @Test
    @DisplayName("Успешная регистрация пользователя c игрой. Проваленный тест для проверки Soft assertions") //библиотека Faker
    public void positiveRegisterWithGamesSoftAssertionsTest() {
        FullUser user = getRandomUserWithGames();
        Response response = userService.register(user)
                .asResponse();
        Info info = response.jsonPath().getObject("info", Info.class);

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(info.getMessage()).as("Сообщение об ошибке было не верное")
                .isEqualTo("fake message");
        softAssertions.assertThat(response.statusCode()).as("Статус код не был 200")
                .isEqualTo(200);
        softAssertions.assertAll();

    }

    @Test
    @DisplayName("Регистрация с существующим логином")
    public void negativeRegisterLoginExistTest() {
        userService.register(randomUser);
        userService.register(randomUser)
                .should(hasStatusCode(400))
                .should(hasMessage("Login already exist"));
    }

    @Test
    @DisplayName("Регистрация без пароля")
    public void negativeRegisterWithoutPasswordTest() {
        randomUser.setPass(null);
        userService.register(randomUser)
                .should(hasStatusCode(400))
                .should(hasMessage("Missing login or password"));
    }

    @Test
    @DisplayName("Успешная авторизация с админской УЗ")
    public void positiveAdminAuthTest(@AdminUser FullUser admin) {
        String token = userService.auth(admin)
                .should(hasStatusCode(200))
                .asJwt();

        Assertions.assertNotNull(token);
    }

    @Test
    @DisplayName("Успешная авторизация нового пользователя")
    public void positiveWithNewAuthUserTest() {
        userService.register(randomUser);

        String token = userService.auth(randomUser)
                .should(hasStatusCode(200))
                .asJwt();

        Assertions.assertNotNull(token);
    }

    @Test
    @DisplayName("Авторизация с неправильным логином и паролем")
    public void negativeAuthTest() {
        userService.auth(randomUser)
                .should(hasStatusCode(401));
    }

    @Test
    @DisplayName("Получение информации о пользователе")
    public void positiveGetUserInfoTest() {
        FullUser user = getAdminUser();
        String token = userService.auth(user).asJwt();
        userService.getUserInfo(token)
                .should(hasStatusCode(200));
    }

    @Test
    @DisplayName("Получение информации о пользователе с невалидным токеном")
    public void negativeGetUserInfoInvalidJwtTokenTest() {
        userService.getUserInfo("fake jwt")
                .should(hasStatusCode(401));
    }

    @Test
    @DisplayName("Получение информации о пользователе без токена")
    public void negativeGetUserInfoWithoutJwtTokenTest() {
        userService.getUserInfo()
                .should(hasStatusCode(401));
    }

    @Test
    @DisplayName("Обновление пароля у пользователя")
    public void positiveChangePasswordTest() {
        String oldPassword = randomUser.getPass();
        userService.register(randomUser);

        String token = userService.auth(randomUser).asJwt();

        String updatedPassValue = "newpassUpdated";

        userService.updatePass(updatedPassValue, token)
                .should(Conditions.hasStatusCode(200))
                .should(Conditions.hasMessage("User password successfully changed"));

        randomUser.setPass(updatedPassValue);

        token = userService.auth(randomUser).should(Conditions.hasStatusCode(200)).asJwt();

        FullUser updatedUser = userService.getUserInfo(token).as(FullUser.class);

        Assertions.assertNotEquals(oldPassword, updatedUser.getPass());
    }

    @Test
    @DisplayName("Смена паролля у админской УЗ")
    public void negativeChangeAdminPasswordTest() {
        FullUser user = getAdminUser();

        String token = userService.auth(user).asJwt();

        String updatedPassValue = "newpassUpdated";
        userService.updatePass(updatedPassValue, token)
                .should(hasStatusCode(400))
                .should(hasMessage("Cant update base users"));
    }

    @Test
    @DisplayName("Удаление админской УЗ")
    public void negativeDeleteAdminTest() {
        FullUser user = getAdminUser();

        String token = userService.auth(user).asJwt();

        userService.deleteUser(token)
                .should(hasStatusCode(400))
                .should(hasMessage("Cant delete base users"));
    }

    @Test
    @DisplayName("Удаление УЗ пользователя")
    public void deleteNewUserTest() {
        userService.register(randomUser);
        String token = userService.auth(randomUser).asJwt();

        userService.deleteUser(token)
                .should(hasStatusCode(200))
                .should(hasMessage("User successfully deleted"));
    }

    @Test
    @DisplayName("Список пользователей равен или больше 3ех")
    public void positiveGetAllUsersTest() {
        List<String> users = userService.getAllUsers().asList(String.class);
        Assertions.assertTrue(users.size() >= 3);
    }
}
