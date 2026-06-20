package com.pdftoolkit.integration;

import com.pdftoolkit.support.TestFiles;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * End-to-end: merge two PDFs, poll until the async job completes, then download the result.
 */
class MergeFlowIntegrationTest extends IntegrationTestBase {

    @Autowired WebApplicationContext context;
    @Autowired ObjectMapper objectMapper;

    private MockMvc mockMvc() {
        return MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void mergeProducesDownloadablePdf() throws Exception {
        MockMvc mvc = mockMvc();
        var a = new MockMultipartFile("files", "a.pdf", "application/pdf", TestFiles.pdf(1));
        var b = new MockMultipartFile("files", "b.pdf", "application/pdf", TestFiles.pdf(2));

        MvcResult submit = mvc.perform(multipart("/api/pdf/merge").file(a).file(b))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status", is("QUEUED")))
                .andReturn();

        String jobId = objectMapper.readTree(submit.getResponse().getContentAsString()).get("id").asText();

        String status = pollUntilTerminal(mvc, jobId);
        assertThat(status).isEqualTo("COMPLETED");

        byte[] downloaded = mvc.perform(get("/api/download/{id}", jobId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();
        assertThat(new String(downloaded, 0, 4)).isEqualTo("%PDF");
    }

    private String pollUntilTerminal(MockMvc mvc, String jobId) throws Exception {
        Instant deadline = Instant.now().plus(Duration.ofSeconds(30));
        while (Instant.now().isBefore(deadline)) {
            MvcResult result = mvc.perform(get("/api/jobs/{id}", jobId)).andReturn();
            JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
            String status = body.get("status").asText();
            if (status.equals("COMPLETED") || status.equals("FAILED")) {
                return status;
            }
            Thread.sleep(250);
        }
        throw new AssertionError("Job did not finish within timeout");
    }
}
