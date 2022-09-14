package me.wani4ka.yadisk;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.wani4ka.yadisk.exceptions.ItemNotFoundException;
import me.wani4ka.yadisk.models.ItemImport;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.text.SimpleDateFormat;

import static org.springframework.test.util.AssertionErrors.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class YadiskApplicationTests {

	private static final ObjectMapper mapper = new ObjectMapper();
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
	private static final String RESULT_OK = "{\"code\":200}";
	private static final String RESULT_NOT_FOUND = "{\"code\":404,\"message\":\"Item not found\"}";
	private static final String RESULT_VALIDATION_FAILED = "{\"code\":400,\"message\":\"Validation Failed\"}";

	private static final ItemImport.Request[] test1Requests = {
			new ItemImport.Request.Builder()
					.addFolder("root", null)
					.build(),
			new ItemImport.Request.Builder()
					.addFolder("foo", "root")
					.addFile("file1", "/file/url1", "foo", 128)
					.addFile("file2", "/file/url2", "foo", 256)
					.build(),
			new ItemImport.Request.Builder()
					.addFolder("bar", "root")
					.addFile("file3", "/file/url3", "bar", 512)
					.addFile("file4", "/file/url4", "bar", 1024)
					.build(),
			new ItemImport.Request.Builder()
					.addFile("file5", "/file/url5", "bar", 64)
					.build(),
	};

	@Autowired
	private MockMvc mockMvc;

	@Test
	@Order(1)
	public void shouldImport() throws Exception {
		for (ItemImport.Request req : test1Requests)
			mockMvc.perform(post("/imports")
							.contentType(MediaType.APPLICATION_JSON)
							.content(mapper.writeValueAsBytes(req)))
					.andDo(print()).andExpect(status().isOk())
					.andExpect(content().json(RESULT_OK));
	}
	
	@Test
	@Order(2)
	public void shouldReturnCorrectTree() throws Exception {
		mockMvc.perform(get("/nodes/{id}", "root"))
				.andDo(print()).andExpect(status().isOk())
				.andExpect(jsonPath("$.type").value("FOLDER"))
				.andExpect(jsonPath("$.id").value("root"))
				.andExpect(jsonPath("$.size").value(1984))
				.andExpect(jsonPath("$.children").isNotEmpty());
	}

	@Test
	@Order(3)
	public void shouldRemoveRootAndChildren() throws Exception {
		mockMvc.perform(delete("/delete/{id}", "root"))
				.andDo(print()).andExpect(status().isOk())
				.andExpect(content().json(RESULT_OK));
		for (ItemImport.Request req : test1Requests)
			for (ItemImport itemImport : req.getItems())
				mockMvc.perform(get("/nodes/{id}", itemImport.getId()))
						.andDo(print())
						.andExpect(status().isNotFound())
						.andExpect(content().json(RESULT_NOT_FOUND))
						.andExpect(result -> assertTrue("No exception is throws", result.getResolvedException() instanceof ItemNotFoundException));
	}

	@Test
	public void shouldDeclineDumbRequests() throws Exception {
		mockMvc.perform(post("/imports")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"a\": \"abacaba\"}"))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(content().json(RESULT_VALIDATION_FAILED));
		mockMvc.perform(post("/imports"))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(content().json(RESULT_VALIDATION_FAILED));
		//noinspection JsonStandardCompliance
		mockMvc.perform(post("/imports")
				.contentType(MediaType.APPLICATION_JSON)
				.content("i am crazy lol"))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(content().json(RESULT_VALIDATION_FAILED));
	}

}
