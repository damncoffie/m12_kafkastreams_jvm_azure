-- Create stream based on enriched hotel data
CREATE STREAM hotel_category (
        hotel_id INT,
        stay_duration_type STRING
        ) WITH (
            KAFKA_TOPIC='expedia-ext',
            VALUE='JSON'
        );

-- Create aggregated table based on previous stream
CREATE TABLE hotel_category_agg AS
    SELECT stay_duration_type,
           COUNT(hotel_id) as total_visits,
           COUNT_DISTINCT(hotel_id) as distinct_hotels
    FROM hotel_category
    GROUP BY stay_duration_type
    emit changes;

-- Query the table
SELECT * FROM hotel_category_agg emit changes;
