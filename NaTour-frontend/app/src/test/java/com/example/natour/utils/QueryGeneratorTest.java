package com.example.natour.utils;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class QueryGeneratorTest {

    QueryGenerator queryGenerator;
    String search;
    String place;
    String duration;
    String difficulty;
    String disabledAccess;
    Map<String, String> filters;

    @Before
    public void setUp() throws Exception {
        queryGenerator = new QueryGenerator();
        search = "Diego";
        place = "Napoli";
        duration = "8";
        difficulty = "3";
        disabledAccess = "true";
    }

    @Test
    public void createFilterQueryNoFilter() {
        filters = null;
        assert (queryGenerator.createFilterQuery(search, filters).equals("SELECT I.id, I.title, I.description FROM Itinerary I WHERE I.private = FALSE AND I.title LIKE '%Diego%';"));
    }

    @Test (expected = NullPointerException.class)
    public void createFilterQueryStringNull() {
        search = null;
        filters = null;
        String query = queryGenerator.createFilterQuery(search, filters);
        fail();
    }

    @Test
    public void createFilterQueryEmptyString() {
        search = "";
        filters = null;
        assert (queryGenerator.createFilterQuery(search, filters).equals("SELECT I.id, I.title, I.description FROM Itinerary I WHERE I.private = FALSE AND I.title LIKE '%%';"));
    }

    @Test
    public void createFilterQueryFullFilters() {
        filters = new HashMap<>();
        filters.put("Place", place);
        filters.put("Duration", duration);
        filters.put("Difficulty", difficulty);
        filters.put("DisabledAccess", disabledAccess);
        assert (queryGenerator.createFilterQuery(search, filters).equals(
                "SELECT I.id, I.title, I.description FROM Itinerary I WHERE I.private = FALSE AND I.title LIKE '%Diego%'"
                + " AND (I.State LIKE '%Napoli%' OR I.Region LIKE '%Napoli%' OR I.City LIKE '%Napoli%')"
                + " AND I.Difficulty <= 3"
                + " AND I.Duration <= 28800"
                + " AND I.access_dis = true;"
        ));
    }

    @Test
    public void createFilterQueryNoPlace() {
        filters = new HashMap<>();
        filters.put("Duration", duration);
        filters.put("Difficulty", difficulty);
        filters.put("DisabledAccess", disabledAccess);
        assert (queryGenerator.createFilterQuery(search, filters).equals(
                "SELECT I.id, I.title, I.description FROM Itinerary I WHERE I.private = FALSE AND I.title LIKE '%Diego%'"
                        + " AND I.Difficulty <= 3"
                        + " AND I.Duration <= 28800"
                        + " AND I.access_dis = true;"
        ));
    }

    @Test
    public void createFilterQueryNoDuration() {
        filters = new HashMap<>();
        filters.put("Place", place);
        filters.put("Difficulty", difficulty);
        filters.put("DisabledAccess", disabledAccess);
        assert (queryGenerator.createFilterQuery(search, filters).equals(
                "SELECT I.id, I.title, I.description FROM Itinerary I WHERE I.private = FALSE AND I.title LIKE '%Diego%'"
                        + " AND (I.State LIKE '%Napoli%' OR I.Region LIKE '%Napoli%' OR I.City LIKE '%Napoli%')"
                        + " AND I.Difficulty <= 3"
                        + " AND I.access_dis = true;"
        ));
    }

    @Test
    public void createFilterQueryNoDifficulty() {
        filters = new HashMap<>();
        filters.put("Place", place);
        filters.put("Duration", duration);
        filters.put("DisabledAccess", disabledAccess);
        assert (queryGenerator.createFilterQuery(search, filters).equals(
                "SELECT I.id, I.title, I.description FROM Itinerary I WHERE I.private = FALSE AND I.title LIKE '%Diego%'"
                        + " AND (I.State LIKE '%Napoli%' OR I.Region LIKE '%Napoli%' OR I.City LIKE '%Napoli%')"
                        + " AND I.Duration <= 28800"
                        + " AND I.access_dis = true;"
        ));
    }

    @Test
    public void createFilterQueryNoDisabledAccess() {
        filters = new HashMap<>();
        filters.put("Place", place);
        filters.put("Duration", duration);
        filters.put("Difficulty", difficulty);
        assert (queryGenerator.createFilterQuery(search, filters).equals(
                "SELECT I.id, I.title, I.description FROM Itinerary I WHERE I.private = FALSE AND I.title LIKE '%Diego%'"
                        + " AND (I.State LIKE '%Napoli%' OR I.Region LIKE '%Napoli%' OR I.City LIKE '%Napoli%')"
                        + " AND I.Difficulty <= 3"
                        + " AND I.Duration <= 28800;"
        ));
    }

    @Test
    public void createFilterQueryDifficultyAndDisablesAccess() {
        filters = new HashMap<>();

        filters.put("Difficulty", difficulty);
        filters.put("DisabledAccess", disabledAccess);
        assert (queryGenerator.createFilterQuery(search, filters).equals(
                "SELECT I.id, I.title, I.description FROM Itinerary I WHERE I.private = FALSE AND I.title LIKE '%Diego%'"
                        + " AND I.Difficulty <= 3"
                        + " AND I.access_dis = true;"
        ));
    }

    @Test
    public void createFilterQueryPlaceAndDuration() {
        filters = new HashMap<>();
        filters.put("Place", place);
        filters.put("Duration", duration);
        assert (queryGenerator.createFilterQuery(search, filters).equals(
                "SELECT I.id, I.title, I.description FROM Itinerary I WHERE I.private = FALSE AND I.title LIKE '%Diego%'"
                        + " AND (I.State LIKE '%Napoli%' OR I.Region LIKE '%Napoli%' OR I.City LIKE '%Napoli%')"
                        + " AND I.Duration <= 28800;"
        ));
    }

    @Test
    public void createFilterQueryPlaceAndDisabledAccess() {
        filters = new HashMap<>();
        filters.put("Place", place);
        filters.put("DisabledAccess", disabledAccess);

        assert (queryGenerator.createFilterQuery(search, filters).equals(
                "SELECT I.id, I.title, I.description FROM Itinerary I WHERE I.private = FALSE AND I.title LIKE '%Diego%'"
                        + " AND (I.State LIKE '%Napoli%' OR I.Region LIKE '%Napoli%' OR I.City LIKE '%Napoli%')"
                        + " AND I.access_dis = true;"
        ));
    }

    @Test
    public void createFilterQueryPlaceAndDifficulty() {
        filters = new HashMap<>();
        filters.put("Place", place);
        filters.put("Difficulty", difficulty);
        assert (queryGenerator.createFilterQuery(search, filters).equals(
                "SELECT I.id, I.title, I.description FROM Itinerary I WHERE I.private = FALSE AND I.title LIKE '%Diego%'"
                        + " AND (I.State LIKE '%Napoli%' OR I.Region LIKE '%Napoli%' OR I.City LIKE '%Napoli%')"
                        + " AND I.Difficulty <= 3;"
        ));
    }

    @Test
    public void createFilterQueryDurationAndDifficulty() {
        filters = new HashMap<>();
        filters.put("Duration", duration);
        filters.put("Difficulty", difficulty);

        assert (queryGenerator.createFilterQuery(search, filters).equals(
                "SELECT I.id, I.title, I.description FROM Itinerary I WHERE I.private = FALSE AND I.title LIKE '%Diego%'"
                        + " AND I.Difficulty <= 3"
                        + " AND I.Duration <= 28800;"
        ));
    }

    @Test
    public void createFilterQueryDurationAndDisabledAccess() {
        filters = new HashMap<>();
        filters.put("Duration", duration);
        filters.put("DisabledAccess", disabledAccess);
        assert (queryGenerator.createFilterQuery(search, filters).equals(
                "SELECT I.id, I.title, I.description FROM Itinerary I WHERE I.private = FALSE AND I.title LIKE '%Diego%'"
                        + " AND I.Duration <= 28800"
                        + " AND I.access_dis = true;"
        ));
    }

    @Test
    public void createFilterQueryPlace() {
        filters = new HashMap<>();
        filters.put("Place", place);
        assert (queryGenerator.createFilterQuery(search, filters).equals(
                "SELECT I.id, I.title, I.description FROM Itinerary I WHERE I.private = FALSE AND I.title LIKE '%Diego%'"
                        + " AND (I.State LIKE '%Napoli%' OR I.Region LIKE '%Napoli%' OR I.City LIKE '%Napoli%');"
        ));
    }

    @Test
    public void createFilterQueryDifficulty() {
        filters = new HashMap<>();
        filters.put("Difficulty", difficulty);
        assert (queryGenerator.createFilterQuery(search, filters).equals(
                "SELECT I.id, I.title, I.description FROM Itinerary I WHERE I.private = FALSE AND I.title LIKE '%Diego%'"
                        + " AND I.Difficulty <= 3;"
        ));
    }

    @Test
    public void createFilterQueryDuration() {
        filters = new HashMap<>();
        filters.put("Duration", duration);
        assert (queryGenerator.createFilterQuery(search, filters).equals(
                "SELECT I.id, I.title, I.description FROM Itinerary I WHERE I.private = FALSE AND I.title LIKE '%Diego%'"
                        + " AND I.Duration <= 28800;"
        ));
    }

    @Test
    public void createFilterQueryDisabledAccess() {
        filters = new HashMap<>();
        filters.put("DisabledAccess", disabledAccess);
        assert (queryGenerator.createFilterQuery(search, filters).equals(
                "SELECT I.id, I.title, I.description FROM Itinerary I WHERE I.private = FALSE AND I.title LIKE '%Diego%'"
                        + " AND I.access_dis = true;"
        ));
    }

    @Test
    public void createFilterQueryEmptyStringPlace() {
        filters = new HashMap<>();
        filters.put("Place", "");

        assert (queryGenerator.createFilterQuery(search, filters).equals(
                "SELECT I.id, I.title, I.description FROM Itinerary I WHERE I.private = FALSE AND I.title LIKE '%Diego%';"
        ));
    }

    @Test (expected = IllegalArgumentException.class)
    public void createFilterQueryNotValidLessDuration() {
        filters = new HashMap<>();
        filters.put("Place", place);
        filters.put("Duration", "0");
        filters.put("Difficulty", difficulty);
        filters.put("DisabledAccess", disabledAccess);

        String query = queryGenerator.createFilterQuery(search, filters);
        fail();

    }

    @Test (expected = IllegalArgumentException.class)
    public void createFilterQueryNotValidLessDifficulty() {
        filters = new HashMap<>();
        filters.put("Place", place);
        filters.put("Duration", duration);
        filters.put("Difficulty", "0");
        filters.put("DisabledAccess", disabledAccess);

        String query = queryGenerator.createFilterQuery(search, filters);
        fail();
    }

    @Test (expected = IllegalArgumentException.class)
    public void createFilterQueryNotValidMoreDuration() {
        filters = new HashMap<>();
        filters.put("Place", place);
        filters.put("Duration", "11");
        filters.put("Difficulty", difficulty);
        filters.put("DisabledAccess", disabledAccess);

        String query = queryGenerator.createFilterQuery(search, filters);
        fail();

    }

    @Test (expected = IllegalArgumentException.class)
    public void createFilterQueryNotValidMoreDifficulty() {
        filters = new HashMap<>();
        filters.put("Place", place);
        filters.put("Duration", duration);
        filters.put("Difficulty", "6");
        filters.put("DisabledAccess", disabledAccess);

        String query = queryGenerator.createFilterQuery(search, filters);
        fail();

    }

}