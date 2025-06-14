package example.cashcard;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CashCardApplicationTests {

	@Autowired
	TestRestTemplate restTemplate;

	@Test
	void  shouldReturnCashCardWhenDataSaved() {
		ResponseEntity<String> response = restTemplate.getForEntity("/cashCards/99", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		//This converts the response String into a JSON-aware object with lots of helper methods.
		DocumentContext documentContext = JsonPath.parse(response.getBody());

		/*
		We expect that when we request a Cash Card with id of 99 a JSON object will be returned with something in the id field.
		 For now, assert that the id is not null.
		 */
		Number id = documentContext.read("$.id");
		assertThat(id).isEqualTo(99);

		Double amount = documentContext.read("$.amount");
		assertThat(amount).isEqualTo(123.45);
	}

	@Test
	void shouldNotReturnCashCardWithUnknownId() {
		ResponseEntity<String> response = restTemplate.getForEntity("/cashCards/1000", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).isBlank();
	}

}
