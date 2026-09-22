package dev.learning.hello;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class HelloApiTest {
    @Autowired MockMvc mvc;

    @Test void createsThenReadsResource() throws Exception {
        var response = mvc.perform(post("/api/tasks").contentType("application/json")
                .content("{\"title\":\"学习依赖注入\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.title").value("学习依赖注入"))
                .andReturn().getResponse();
        mvc.perform(get(response.getHeader("Location")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.title").value("学习依赖注入"));
    }

    @Test void blankTitleIsAClientError() throws Exception {
        mvc.perform(post("/api/tasks").contentType("application/json").content("{\"title\":\"  \"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
    }

    @Test void missingResourceIs404() throws Exception {
        mvc.perform(get("/api/tasks/" + UUID.randomUUID()))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404));
    }
}
