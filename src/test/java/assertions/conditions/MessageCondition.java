package assertions.conditions;

import assertions.Condition;
import io.restassured.response.ValidatableResponse;
import lombok.RequiredArgsConstructor;
import models.swaggerModels.Info;
import org.junit.jupiter.api.Assertions;

import static org.hamcrest.core.IsEqual.equalTo;

@RequiredArgsConstructor
public class MessageCondition implements Condition {

    private final String expectedMessage;

    @Override
    public void check(ValidatableResponse response) {
        Info info = response.extract().jsonPath().getObject("info", Info.class);
        Assertions.assertEquals(expectedMessage,info.getMessage());

      //2 вариант response.body("info.message", equalTo(expectedMessage));
    }

}
