package tests.swaggerTests;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import listeners.AdminUserResolver;
import models.swaggerModels.FullUser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import services.UserService;
import utils.CustomTpl;

import static utils.RandomTestData.getRandomUser;

@ExtendWith(AdminUserResolver.class)
public class BaseApiTest {


    protected static UserService userService;
    protected FullUser randomUser;

    @BeforeAll
    public static void setUp() {
        RestAssured.baseURI = "http://85.192.34.140:8080/api";
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter(),
                CustomTpl.customLogFilter().withCustomTemplates());
        userService = new UserService();
    }

    @BeforeEach
    public void initTestUser() {
        randomUser = getRandomUser();
    }
}