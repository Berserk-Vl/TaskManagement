package ru.sb.TaskManagement;

import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.regex.Pattern;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTests {

    @Autowired
    private MockMvc mvc;
    private static JacksonJsonProvider jsonProvider;
    private final static String ERROR_MESSAGE_PATH = "$.['error message']";

    @BeforeAll
    public static void initialization() {
        jsonProvider = new JacksonJsonProvider();
    }

    @Test
    public void login() throws Exception {
        Object map = jsonProvider.createMap();
        jsonProvider.setProperty(map, "email", "admin@sb.ru");
        jsonProvider.setProperty(map, "password", "admin");
        mvc.perform(MockMvcRequestBuilders
                        .post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonProvider.toJson(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value(new BaseMatcher<>() {
                    @Override
                    public void describeTo(Description description) {
                        description.appendText("Matching this pattern: .+\\..+\\..+");
                    }

                    @Override
                    public boolean matches(Object actual) {
                        return Pattern.compile(".+\\..+\\..+").matcher((String) actual).matches();
                    }
                }));
    }

    @Test
    public void loginUnknownCredentials() throws Exception {
        Object map = jsonProvider.createMap();
        jsonProvider.setProperty(map, "email", "user@sb.ru");
        jsonProvider.setProperty(map, "password", "123");
        errorRequest(map, status().isForbidden(), "ERROR[403]: Authentication failed.");
    }

    @Test
    public void loginEmailNotProvided() throws Exception {
        Object noEmailMap = jsonProvider.createMap();
        jsonProvider.setProperty(noEmailMap, "password", "123");
        errorRequest(noEmailMap, status().isBadRequest(), "ERROR[400]: The Email and Password fields are required and cannot be null.");
    }

    @Test
    public void loginPasswordNotProvided() throws Exception {
        Object noPasswordMap = jsonProvider.createMap();
        jsonProvider.setProperty(noPasswordMap, "email", "user@sb.ru");
        errorRequest(noPasswordMap, status().isBadRequest(), "ERROR[400]: The Email and Password fields are required and cannot be null.");
    }

    @Test
    public void loginEmailNull() throws Exception {
        Object nullEmailMap = jsonProvider.createMap();
        jsonProvider.setProperty(nullEmailMap, "email", null);
        jsonProvider.setProperty(nullEmailMap, "password", "123");
        errorRequest(nullEmailMap, status().isBadRequest(), "ERROR[400]: The Email and Password fields are required and cannot be null.");
    }

    @Test
    public void loginPasswordNull() throws Exception {
        Object nullPasswordMap = jsonProvider.createMap();
        jsonProvider.setProperty(nullPasswordMap, "email", "admin@sb.ru");
        jsonProvider.setProperty(nullPasswordMap, "password", null);
        errorRequest(nullPasswordMap, status().isBadRequest(), "ERROR[400]: The Email and Password fields are required and cannot be null.");
    }

    @Test
    public void loginEmailAndPasswordNotProvided() throws Exception {
        errorRequest(jsonProvider.createMap(), status().isBadRequest(), "ERROR[400]: The Email and Password fields are required and cannot be null.");
    }

    @Test
    public void loginNoBody() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .post("/user/login"))
                .andExpect(status().isBadRequest());
    }

    private void errorRequest(Object body,
                              ResultMatcher status, String errorMessage) throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonProvider.toJson(body)))
                .andExpect(status)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(ERROR_MESSAGE_PATH).value(errorMessage));
    }
}