package dev.learning.tasks;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TaskApiTest {
    @Autowired MockMvc mvc;

    @Test void unauthenticatedReadIs401() throws Exception {
        mvc.perform(get("/api/tasks")).andExpect(status().isUnauthorized());
    }
    @Test void writeRequiresCsrfEvenWithValidRole() throws Exception {
        mvc.perform(post("/api/tasks").with(httpBasic("writer", "writer-local-only"))
                .contentType("application/json").content("{\"title\":\"任务\"}"))
                .andExpect(status().isForbidden());
    }
    @Test void readerCannotWriteEvenWithCsrf() throws Exception {
        mvc.perform(post("/api/tasks").with(user("reader").roles("READER")).with(csrf())
                .contentType("application/json").content("{\"title\":\"任务\"}"))
                .andExpect(status().isForbidden());
    }
    @Test void writerCreatesResource() throws Exception {
        mvc.perform(post("/api/tasks").with(user("writer").roles("WRITER")).with(csrf())
                .contentType("application/json").content("{\"title\":\"创建成功\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("TODO"));
    }
    @Test void invalidBodyAndMissingResourceHaveProblemDetails() throws Exception {
        mvc.perform(post("/api/tasks").with(user("writer").roles("WRITER")).with(csrf())
                .contentType("application/json").content("{\"title\":\" \"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
        mvc.perform(get("/api/tasks/" + UUID.randomUUID()).with(user("reader").roles("READER")))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404));
    }
    @Test void paginationRejectsExcessiveSize() throws Exception {
        mvc.perform(get("/api/tasks?size=101").with(user("reader").roles("READER")))
                .andExpect(status().isBadRequest());
    }
    @Test void healthIsPublicButMetricsRequireOps() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
        mvc.perform(get("/actuator/metrics").with(user("reader").roles("READER"))).andExpect(status().isForbidden());
    }
}
