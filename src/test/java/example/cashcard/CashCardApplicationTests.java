package example.cashcard;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CashCardApplicationTests {


    @Test
    void contextLoads() {
    }

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void shouldReturnACashCardWhenDataIsSaved(){
        ResponseEntity<String> response = restTemplate.getForEntity("/cashcards/99", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        DocumentContext documentContext = JsonPath.parse(response.getBody());
        Number id = documentContext.read("$.id");
        assertThat(id).isNotNull();

    }

    @Test
    @DirtiesContext
    /* Testing POST operation
    * */
    void shouldCreateANewCashCard(){
        CashCard cashCard = new CashCard(null, 250.00);
        ResponseEntity<Void> createResponse = restTemplate.postForEntity("/cashcards",cashCard, Void.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        URI locationOfNewCashCard = createResponse.getHeaders().getLocation();
        ResponseEntity<String> getResponse = restTemplate.getForEntity(locationOfNewCashCard, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext dc = JsonPath.parse(getResponse.getBody());
        Number id = dc.read("$.id");
        Double amount = dc.read("$.amount");
        assertThat(id).isNotNull();
        assertThat(amount).isEqualTo(250.00);
    }

    @Test
    void shouldReturnAllCashCardsWhenListIsRequested(){
        ResponseEntity<String> getResponse = restTemplate.getForEntity("/cashcards", String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext dc = JsonPath.parse(getResponse.getBody());
        int cashCardCount = dc.read("$.length()");
        assertThat(cashCardCount).isEqualTo(3);

        JSONArray ids = dc.read("$..id");
        JSONArray amounts = dc.read("$..amount");
        assertThat(ids).containsExactlyInAnyOrder(99, 100, 101);
        assertThat(amounts).containsExactlyInAnyOrder(123.45, 1.0, 150.0);
    }

    @Test
    void shouldReturnAPageOfCashCards(){
        ResponseEntity<String> getResponse = restTemplate.getForEntity("/cashcards?page=0&size=1", String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext dc = JsonPath.parse(getResponse.getBody());
        JSONArray page = dc.read("$[*]");
        assertThat(page.size()).isEqualTo(1);
    }

    @Test
    void shouldReturnASortedPageOfCashCardsAscending(){
        ResponseEntity<String> getResponse = restTemplate.getForEntity("/cashcards?page=0&size=1&sort=amount,asc",String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext dc = JsonPath.parse(getResponse.getBody());
        JSONArray amounts = dc.read("$[*].amount");
        assertThat(amounts).containsExactly(1.0);
    }

    @Test
    void shouldReturnASortedPageofCashCardsWithNoParametersAndUseDefaultValues(){
        ResponseEntity<String> response = restTemplate.getForEntity("/cashcards", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);



        DocumentContext dc= JsonPath.parse(response.getBody());
        JSONArray page = dc.read("[*]");
        assertThat(page.size()).isEqualTo(3);
        JSONArray ids = dc.read("[*].id");
        assertThat(ids).containsExactly(100,99,101);



    }

}
