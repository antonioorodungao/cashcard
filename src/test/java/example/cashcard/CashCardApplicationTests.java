package example.cashcard;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.apache.coyote.Response;
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
import java.security.Principal;

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
        ResponseEntity<String> response = restTemplate.withBasicAuth("antonio", "123")
                .getForEntity("/cashcards/99", String.class);

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
        CashCard cashCard =new CashCard(null, 250.00, null);
        ResponseEntity<Void> createResponse = restTemplate.withBasicAuth("antonio", "123")
                .postForEntity("/cashcards",cashCard, Void.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        URI locationOfNewCashCard = createResponse.getHeaders().getLocation();
        ResponseEntity<String> getResponse = restTemplate.withBasicAuth("antonio", "123")
                .getForEntity(locationOfNewCashCard, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext dc = JsonPath.parse(getResponse.getBody());
        Number id = dc.read("$.id");
        Double amount = dc.read("$.amount");
        assertThat(id).isNotNull();
        assertThat(amount).isEqualTo(250.00);
    }

    @Test
    void shouldReturnAllCashCardsWhenListIsRequested(){
        ResponseEntity<String> getResponse = restTemplate.withBasicAuth("antonio", "123")
                .getForEntity("/cashcards", String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext dc = JsonPath.parse(getResponse.getBody());
        int cashCardCount = dc.read("$.length()");
        assertThat(cashCardCount).isEqualTo(1);

        JSONArray ids = dc.read("$..id");
        JSONArray amounts = dc.read("$..amount");
        assertThat(ids).containsExactlyInAnyOrder(99);
        assertThat(amounts).containsExactlyInAnyOrder(123.45);
    }

    @Test
    void shouldReturnAPageOfCashCards(){
        ResponseEntity<String> getResponse = restTemplate.withBasicAuth("antonio", "123")
                .getForEntity("/cashcards?page=0&size=1", String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext dc = JsonPath.parse(getResponse.getBody());
        JSONArray page = dc.read("$[*]");
        assertThat(page.size()).isEqualTo(1);
    }

    @Test
    void shouldReturnASortedPageOfCashCardsAscending(){
        ResponseEntity<String> getResponse = restTemplate.withBasicAuth("antonio", "123")
                .getForEntity("/cashcards?page=0&size=1&sort=amount,asc",String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext dc = JsonPath.parse(getResponse.getBody());
        JSONArray amounts = dc.read("$[*].amount");
        assertThat(amounts).containsExactly(123.45);
    }

    @Test
    void shouldReturnASortedPageofCashCardsWithNoParametersAndUseDefaultValues(){
        ResponseEntity<String> response = restTemplate.withBasicAuth("antonio", "123")
                .getForEntity("/cashcards", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        DocumentContext dc= JsonPath.parse(response.getBody());
        JSONArray page = dc.read("[*]");
        assertThat(page.size()).isEqualTo(1);
        JSONArray ids = dc.read("[*].id");
        assertThat(ids).containsExactly(99);
    }

    @Test
    void shouldNotReturnACashCardWhenUsingBadCredentials(){
        ResponseEntity<String> response = restTemplate.withBasicAuth("baduser", "123")
                .getForEntity("/cashcards", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldNotReturnACashCardOnlyOfTheOwner(){
        ResponseEntity<String> response = restTemplate.withBasicAuth("antonio", "123")
                .getForEntity("/cashcards", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext dc = JsonPath.parse(response.getBody());
        JSONArray id = dc.read("[*].id");
        assertThat(id).containsExactly(99);


    }

    @Test
    void shouldRejectUsersWhoAreNotCardOwners(){
        ResponseEntity<String> response = restTemplate.withBasicAuth("hank", "987")
                .getForEntity("/cashcards", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }


    /**
     * UPDATE URI Testing
     */
    @Test
    @DirtiesContext
    void shouldUpdateAnExistingCashCard(){
        CashCard cashCardUpdate = new CashCard(null, 19.99, null);
        HttpEntity<CashCard> request= new HttpEntity<CashCard>(cashCardUpdate);
        ResponseEntity<String> response = restTemplate.withBasicAuth("antonio", "123")
                .exchange("/cashcards/99", HttpMethod.PUT, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        DocumentContext dc = JsonPath.parse(response.getBody());

        Number id = dc.read("$.id");
        Double amount = dc.read("$.amount");
        assertThat(id).isEqualTo(99);
        assertThat(amount).isEqualTo(19.99);
    }

//    @Test
//    void shouldNotUpdateACashCardThatDoesNotExist(){
//        CashCard unknownCard = new CashCard(null, 99.99, null);
//        HttpEntity<CashCard>  request = new HttpEntity<>(unknownCard);
//        ResponseEntity<Void> response = restTemplate.withBasicAuth("antonio", "123")
//        .exchange("/cashcard/9999", HttpMethod.PUT, request, Void.class);
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
//
//    }

    /**
     *
     */
    @Test
    void shouldNotUpdateACashCardThatDoesNotExist(){
        CashCard unknownCard = new CashCard(null, 99.99, null);
        HttpEntity<CashCard>  request = new HttpEntity<>(unknownCard);
        ResponseEntity<Void> response = restTemplate.withBasicAuth("antonio", "123")
        .exchange("/cashcard/9999", HttpMethod.PUT, request, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

    }

    /**
     * Delete HTTP Method Testing
     */
    @Test
    @DirtiesContext
    void shouldDeleteAnExistingCashCard(){
//        ResponseEntity<Void> deleteResponse = restTemplate.delete("/cashcards/99", Void.class);
        ResponseEntity<Void> deleteResponse = restTemplate.withBasicAuth("antonio", "123")
                .exchange("/cashcards/99", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    /**
     * Delete HTTP Method Testing
     */
    @Test
    @DirtiesContext
    void shouldReturnErrorWhenDeletingANonExistentCard(){
//        ResponseEntity<Void> deleteResponse = restTemplate.delete("/cashcards/99", Void.class);
        ResponseEntity<Void> deleteResponse = restTemplate.withBasicAuth("Antonio", "123")
                .exchange("/cashcards/9999", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }


    @Test
    @DirtiesContext
    void shouldNotAllowDeletefForInvalidUsers(){
//        ResponseEntity<Void> deleteResponse = restTemplate.delete("/cashcards/99", Void.class);
        ResponseEntity<Void> deleteResponse = restTemplate.withBasicAuth("hank", "987")
                .exchange("/cashcards/99", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DirtiesContext
    void shouldNotAllowDeletionOfCashCardsTheyDoNotOwn(){
        ResponseEntity<Void> deleteResponse = restTemplate.withBasicAuth("Antonio", "123")
                .exchange("/cashcards/101", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

}
