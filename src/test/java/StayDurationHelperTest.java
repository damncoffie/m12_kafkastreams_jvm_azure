import com.epam.bd201.helper.StayDurationHelper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StayDurationHelperTest {

    private static final Gson GSON = new Gson();

    // ERROR CASES
    @Test
    public void whenJsonWithoutDates_thenErrorTypeIsSet() {
        // GIVEN
        String jsonString = "{}";

        // WHEN
        String result = StayDurationHelper.setDurationType(jsonString);
        String resultType = extractType(result);

        // THEN
        assertEquals("Erroneous data", resultType);
    }

    @Test
    public void whenJsonWithoutCheckInDate_thenErrorTypeIsSet() {
        // GIVEN
        String jsonString = "{\"srch_co\":\"2017-09-01\"}";

        // WHEN
        String result = StayDurationHelper.setDurationType(jsonString);
        String resultType = extractType(result);

        // THEN
        assertEquals("Erroneous data", resultType);
    }

    @Test
    public void whenJsonWithoutCheckOutDate_thenErrorTypeIsSet() {
        // GIVEN
        String jsonString = "{\"srch_ci\":\"2017-09-01\"}";

        // WHEN
        String result = StayDurationHelper.setDurationType(jsonString);
        String resultType = extractType(result);

        // THEN
        assertEquals("Erroneous data", resultType);
    }

    @Test
    public void whenCheckInDateIsIncorrect_thenErrorTypeIsSet() {
        // GIVEN
        String jsonString = "{\"srch_co\":\"2017-09-01\", \"srch_ci\":\"error\"}";

        // WHEN
        String result = StayDurationHelper.setDurationType(jsonString);
        String resultType = extractType(result);

        // THEN
        assertEquals("Erroneous data", resultType);
    }

    @Test
    public void whenCheckOutDateIsIncorrect_thenErrorTypeIsSet() {
        // GIVEN
        String jsonString = "{\"srch_co\":\"error\", \"srch_ci\":\"2017-09-01\"}";

        // WHEN
        String result = StayDurationHelper.setDurationType(jsonString);
        String resultType = extractType(result);

        // THEN
        assertEquals("Erroneous data", resultType);
    }

    @Test
    public void whenDatesAreEqual_thenErrorTypeIsSet() {
        // GIVEN
        String jsonString = "{\"srch_co\":\"2017-09-01\", \"srch_ci\":\"2017-09-01\"}";

        // WHEN
        String result = StayDurationHelper.setDurationType(jsonString);
        String resultType = extractType(result);

        // THEN
        assertEquals("Erroneous data", resultType);
    }

    // HAPPY PATHS
    @Test
    public void whenDaysDiffIsBetween0And4_thenShortTypeIsSet() {
        // GIVEN
        String jsonString = "{\"srch_ci\":\"2017-09-01\", \"srch_co\":\"2017-09-04\"}";

        // WHEN
        String result = StayDurationHelper.setDurationType(jsonString);
        String resultType = extractType(result);

        // THEN
        assertEquals("Short stay", resultType);
    }

    @Test
    public void whenDaysDiffIsBetween5And10_thenStandardTypeIsSet() {
        // GIVEN
        String jsonString = "{\"srch_ci\":\"2017-09-01\", \"srch_co\":\"2017-09-06\"}";

        // WHEN
        String result = StayDurationHelper.setDurationType(jsonString);
        String resultType = extractType(result);

        // THEN
        assertEquals("Standard stay", resultType);
    }

    @Test
    public void whenDaysDiffIsBetween11And14_thenStandardExtendedTypeIsSet() {
        // GIVEN
        String jsonString = "{\"srch_ci\":\"2017-09-01\", \"srch_co\":\"2017-09-13\"}";

        // WHEN
        String result = StayDurationHelper.setDurationType(jsonString);
        String resultType = extractType(result);

        // THEN
        assertEquals("Standard extended stay", resultType);
    }

    @Test
    public void whenDaysDiffIsMoreThan14_thenLongTypeIsSet() {
        // GIVEN
        String jsonString = "{\"srch_ci\":\"2017-09-01\", \"srch_co\":\"2017-09-29\"}";

        // WHEN
        String result = StayDurationHelper.setDurationType(jsonString);
        String resultType = extractType(result);

        // THEN
        assertEquals("Long stay", resultType);
    }

    private String extractType(String json) {
        JsonObject jsonObject = GSON.fromJson(json, JsonObject.class);
        return jsonObject.get("stay_duration_type").getAsString();
    }
}
