package com.epam.bd201.helper;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.isNull;

public class StayDurationHelper {

    private static final Logger LOGGER = LogManager.getLogger(StayDurationHelper.class);

    private static final Gson GSON = new Gson();
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final String STAY_DURATION_PROP_NAME = "stay_duration_type";

    public StayDurationHelper() {
        SIMPLE_DATE_FORMAT.setLenient(false);
    }

    public static String setDurationType(String jsonString) {
        JsonObject jsonObject = GSON.fromJson(jsonString, JsonObject.class);
        long days = getStayDurationDays(jsonObject);

        if (days == 0 || days < 0) {
            jsonObject.addProperty(STAY_DURATION_PROP_NAME, DurationType.ERROR.value);
        } else if (days <= 4) {
            jsonObject.addProperty(STAY_DURATION_PROP_NAME, DurationType.SHORT.value);
        } else if (days <= 10) {
            jsonObject.addProperty(STAY_DURATION_PROP_NAME, DurationType.STANDARD.value);
        } else if (days <= 14) {
            jsonObject.addProperty(STAY_DURATION_PROP_NAME, DurationType.STANDARD_EXT.value);
        } else {
            jsonObject.addProperty(STAY_DURATION_PROP_NAME, DurationType.LONG.value);
        }

        return jsonObject.toString();
    }

    private static long getStayDurationDays(JsonObject jsonObject) {
        JsonElement checkInDateElement = jsonObject.get("srch_ci");
        JsonElement checkOutDateElement = jsonObject.get("srch_co");

        if (isNull(checkInDateElement) || isNull(checkOutDateElement)) return -1;

        String checkInDateString = checkInDateElement.getAsString();
        Date checkInDate;
        try {
            checkInDate = SIMPLE_DATE_FORMAT.parse(checkInDateString);
        } catch (ParseException e) {
            LOGGER.error("Failed to parse date string: " + checkInDateString);
            return -1;
        }

        String checkOutDateString = checkOutDateElement.getAsString();
        Date checkOutDate;
        try {
            checkOutDate = SIMPLE_DATE_FORMAT.parse(checkOutDateString);
        } catch (ParseException e) {
            LOGGER.error("Failed to parse date string: " + checkOutDateString);
            return -1;
        }

        long diffInMillis = Math.abs(checkOutDate.getTime() - checkInDate.getTime());
        return TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);
    }

    enum DurationType {
        ERROR("Erroneous data"),
        SHORT("Short stay"),
        STANDARD("Standard stay"),
        STANDARD_EXT("Standard extended stay"),
        LONG("Long stay");

        public final String value;

        DurationType(String value) {
            this.value = value;
        }
    }
}
