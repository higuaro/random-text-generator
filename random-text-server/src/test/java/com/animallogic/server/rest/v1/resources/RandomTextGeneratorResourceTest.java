package com.animallogic.server.rest.v1.resources;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class RandomTextGeneratorResourceTest {
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext wac;


    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    @Tag("integration")
    @DisplayName("Should test the happy path, that is, upload a corpus text file and get random text")
    public void testHappyPath() throws Exception {
        String corpusText = loremIpsum();
        MockMultipartFile testFile = new MockMultipartFile("file", "filename.txt", "text/plain", corpusText.getBytes());

        MvcResult result = mockMvc
                .perform(
                        fileUpload("/v1/random/upload-text-file")
                                .file(testFile)
                                .param("prefix-size", "3")
                                .param("ignore-extra-spaces", "true")
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(content().contentType(MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8"))
                .andExpect(content().string(not(corpusText)))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("integration")
    @DisplayName("Should cover all BAD_REQUEST scenarios")
    public void badRequestCases() throws Exception {
        String corpusText = loremIpsum();

        MockMultipartFile wrongFile1 = new MockMultipartFile("WRONG-FILE-NAME", "filename.txt", "text/plain", corpusText.getBytes());
        MockMultipartFile wrongFile2 = new MockMultipartFile("file", "filename.txt", "text/xml", corpusText.getBytes());
        MockMultipartFile goodFile = new MockMultipartFile("file", "filename.txt", "text/plain", corpusText.getBytes());

        // Wrong file parameter name
        mockMvc.perform(
                        fileUpload("/v1/random/upload-text-file")
                                .file(wrongFile1)
                                .param("prefix-size", "2")
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(status().isBadRequest());

        // Wrong file type
        MvcResult result = mockMvc.
                perform(
                        fileUpload("/v1/random/upload-text-file")
                                .file(wrongFile2)
                                .param("prefix-size", "3")
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(request().asyncStarted())
                .andReturn();
        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isBadRequest());

        // Wrong prefix-size
        mockMvc.perform(
                        fileUpload("/v1/random/upload-text-file")
                                .file(goodFile)
                                .param("prefix-size", "WRONG!!")
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(status().isBadRequest());

        // Missing prefix-size
        mockMvc.perform(
                        fileUpload("/v1/random/upload-text-file")
                                .file(goodFile)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(status().isBadRequest());

        // Wrong ignore-extra-spaces
        mockMvc.perform(
                        fileUpload("/v1/random/upload-text-file")
                                .file(goodFile)
                                .param("prefix-size", "4")
                                .param("ignore-extra-spaces", "WRONG!!")
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(status().isBadRequest());
    }

    private String loremIpsum() {
        return "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse sit amet nulla neque. Cras " +
                "consectetur nec purus at tristique. Etiam euismod, tellus vel maximus porta, urna nulla facilisis " +
                "magna, sed pellentesque lectus leo nec mauris. Quisque volutpat aliquet sollicitudin. Nulla ornare " +
                "pretium lectus eget dignissim. Praesent hendrerit molestie sollicitudin. Sed vel mi vitae libero " +
                "aliquet ultricies imperdiet id erat.\n" +
                "\n" +
                "Nam interdum velit a nisi rutrum suscipit. Donec sit amet nunc tortor. Cras accumsan mauris aliquam " +
                "lacus suscipit, in luctus leo convallis. Suspendisse tincidunt vel quam eu gravida. Cras quis metus " +
                "risus. Donec sit amet bibendum magna. Vivamus ut eleifend turpis. Phasellus pellentesque, diam " +
                "vitae faucibus sollicitudin, tellus lectus consequat odio, et laoreet est arcu nec quam. Nulla " +
                "congue, augue quis dictum sagittis, eros orci auctor massa, sit amet suscipit odio enim nec dolor. " +
                "Donec mollis fermentum libero in vestibulum. Nulla viverra varius ipsum sit amet varius. Aenean " +
                "mollis, velit quis suscipit vestibulum, neque risus pellentesque massa, vitae aliquam tellus neque " +
                "non risus. Nam et lectus dolor.";
    }

}