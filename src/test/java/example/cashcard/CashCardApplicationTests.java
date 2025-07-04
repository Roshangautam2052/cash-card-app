package example.cashcard;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import example.cashcard.model.CashCard;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CashCardApplicationTests {

	@Autowired
	TestRestTemplate restTemplate;

	@Test
	void  shouldReturnCashCardWhenDataSaved() {

		ResponseEntity<String> response = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.getForEntity("/cashcards/99", String.class);

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
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.getForEntity("/cashcards/1000", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).isBlank();
	}


	@Test
	@DirtiesContext
	void shouldCreateANewCashCard() {
		CashCard newCashCard = new CashCard(null, 250.00, "sarah1");
		ResponseEntity<Void> createResponse = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.postForEntity("/cashcards",newCashCard, Void.class);
		assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

		URI locationOfNewCashCard = createResponse.getHeaders().getLocation();
		ResponseEntity<String>getResponse = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.getForEntity(locationOfNewCashCard, String.class);

		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
		Number id = documentContext.read("$.id");
		Double amount = documentContext.read("$.amount");

		assertThat(id).isNotNull();
		assertThat(amount).isEqualTo(250.00);
	}

	@Test
	@DirtiesContext
	void shouldReturnAllCashCardsWhenListIsRequested(){
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.getForEntity("/cashcards", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		int cashCardCount = documentContext.read("$.length()");
		assertThat(cashCardCount).isEqualTo(4);

		JSONArray ids = documentContext.read("$..id");
		assertThat(ids).containsExactlyInAnyOrder(99,100,101, 102);

		JSONArray amounts = documentContext.read("$..amount");
		assertThat(amounts).containsExactlyInAnyOrder(123.45, 1.00, 150.00, 200.00);
	}

	@Test
	void shouldReturnAPageOfCashCards() {
		ResponseEntity<String>response = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.getForEntity("/cashcards?page=0&size=1", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		JSONArray page = documentContext.read("$[*]");
		assertThat(page.size()).isEqualTo(1);
	}

	@Test
	void shouldReturnASortedPageOfCashCards() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.getForEntity("/cashcards?page=0&size=1&sort=amount,desc", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		JsonPath.parse(response.getBody());
		JSONArray read = documentContext.read("$[*]");
		assertThat(read.size()).isEqualTo(1);

		double amount = documentContext.read("$[0].amount");
		assertThat(amount).isEqualTo(200.00);
	}

	@Test
	void shouldReturnASortedPageOfCashCardsWithNoParametersAndUseDefaultValues() {
		ResponseEntity<String>response = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.getForEntity("/cashcards", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		JSONArray page = documentContext.read("$[*]");
		assertThat(page.size()).isEqualTo(4);

		JSONArray amounts = documentContext.read("$..amount");
		assertThat(amounts).containsExactly(1.0, 123.45, 150.00, 200.00);
	}

	@Test
	void shouldReturnACashCardWhenDataIsSaved() {
		ResponseEntity<String>response = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.getForEntity("/cashcards/99", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	void shouldNotReturnACashCardWhenUsingBadCredentials(){
		ResponseEntity<String>response = restTemplate
				.withBasicAuth("BAD-USER", "abc123")
				.getForEntity("/cashcards", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);


		ResponseEntity<String>response2 = restTemplate
				.withBasicAuth("sarah1", "BAD-PASSWORD")
				.getForEntity("/cashcards", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);


	}
	@Test
	@DirtiesContext
	void shouldUpdateAnExistingCashCard(){
		CashCard cashCardUpdate = new CashCard(null, 19.99, null);
		HttpEntity<CashCard> request = new HttpEntity<>(cashCardUpdate);
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.exchange("/cashcards/99",
				HttpMethod.PUT, request, Void.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		ResponseEntity<String> getResponse = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.getForEntity("/cashcards/99", String.class);

		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
		Number id = documentContext.read("$.id");
		Double amount = documentContext.read("$.amount");
		assertThat(id).isEqualTo(99);
		assertThat(amount).isEqualTo(19.99);
	}

	@Test
	void shouldNotUpdateCashCardThatDoesNotExist(){
		CashCard unknownCard= new CashCard(null, 19.99, null);
		HttpEntity<CashCard> request = new HttpEntity<>(unknownCard);
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.exchange("/cashcards/99999",
				HttpMethod.PUT, request, Void.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void shouldNotUpdateACashCardThatIsOwnedBySomeoneElse(){
		CashCard kumarsCard = new CashCard(null, 333.33, null);
		HttpEntity<CashCard> request = new HttpEntity<>(kumarsCard);
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.exchange("/cashcards/102", HttpMethod.PUT, request, Void.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	@DirtiesContext
	void shouldDeleteAnExistingCashCard() {
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.exchange("/cashcards/99", HttpMethod.DELETE, null, Void.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		ResponseEntity<String>getResponse = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.getForEntity("/cashcards/99", String.class);

		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void shouldNotDeleteACashCardThatDoesNotExists() {
		ResponseEntity<Void> deleteResponse = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.exchange("/cashcards/99999", HttpMethod.DELETE, null, Void.class);

		assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void shouldNotDeleteACashCardWhenTheyAreNotTheOwner() {
		ResponseEntity<Void> deleteResponse = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.exchange("/cashcards/102", HttpMethod.DELETE, null, Void.class);

		assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void shouldNotAllowDeletionOfCashCardTheyDoNotOwn(){
		ResponseEntity<String>response = restTemplate
				.withBasicAuth("kumar2", "xyz789")
				.getForEntity("/cashcards/102", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}




}
