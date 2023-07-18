package example.cashcard;

import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class CashCardJsonTest {

    @Autowired
    private JacksonTester<CashCard> json;
    @Autowired
    private JacksonTester<CashCard[]> jsonlist;

    CashCard cashCards[] = Arrays.array(new CashCard(99L, 123.45),
            new CashCard(100L, 1.00),
            new CashCard(101L, 150.00));

    CashCard cashCard = new CashCard(99L, 123.45);

    @Test
    public void cashCardSerializationTest() throws IOException {
        assertThat(json.write(cashCard)).isStrictlyEqualToJson("single.json");
        assertThat(json.write(cashCard)).hasJsonPathNumberValue("@.id" );
        assertThat(json.write(cashCard)).extractingJsonPathNumberValue("@.id").isEqualTo(99);
        assertThat(json.write(cashCard)).hasJsonPathNumberValue("@.amount");
        assertThat(json.write(cashCard)).extractingJsonPathNumberValue("@.amount").isEqualTo(123.45);

    }

    @Test
    public void cashCardListSerializationTest() throws IOException{

        assertThat(jsonlist.write(cashCards)).isStrictlyEqualToJson("list.json");
        assertThat(jsonlist.write(cashCards)).extractingJsonPathValue("@[2].id").isEqualTo(101);
        assertThat(json.write(cashCards[2])).extractingJsonPathValue("@.id").isEqualTo(101);
    }

    @Test
    public void cashCardDeserializationTest() throws IOException{
        String expected = """
                {
                "id": 99,
                "amount":123.45
                }
                """;
        assertThat(json.parse(expected)).isEqualTo(cashCard);
        assertThat(json.parseObject(expected).id()).isEqualTo(99);
        assertThat(json.parseObject(expected).amount()).isEqualTo(123.45);
    }

    @Test
    public void cashCardListDeserializationTest() throws IOException{
        String expected = """
                [
                  { "id": 99, "amount": 123.45 },
                  { "id": 100, "amount": 1.0 },
                  { "id": 101, "amount": 150.0 }
                ]
                """;

        assertThat(jsonlist.parse(expected)).isEqualTo(cashCards);
        assertThat(jsonlist.parse(expected).getObject()[1].amount()).isEqualTo(1.0);
//        assertThat((json.parseObject(expected))[1])
    }



}
