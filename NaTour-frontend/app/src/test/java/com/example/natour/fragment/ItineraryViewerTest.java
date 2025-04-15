package com.example.natour.fragment;

import static org.junit.Assert.*;

import android.util.Log;

import com.example.natour.entity.Itinerary;

import org.junit.Before;
import org.junit.Test;

public class ItineraryViewerTest {

    ItineraryViewer itineraryViewer;
    String state;
    String region;
    String city;

    @Before
    public void setUp() throws Exception {
        state = null;
        region = null;
        city = null;
        itineraryViewer = new ItineraryViewer();
    }

    @Test
    public void buildLocationStringComplete() {
        state = "Italia";
        region = "Campania";
        city = "Napoli";
        String build = itineraryViewer.buildLocationString(state, region, city);
        assert(build.equals("Napoli, Campania, Italia"));
    }

    @Test
    public void buildLocationStringNoCity() {
        state = "Italia";
        region = "Campania";
        String build = itineraryViewer.buildLocationString(state, region, city);
        assert(build.equals("Campania, Italia"));
    }

    @Test
    public void buildLocationStringNoRegion() {
        state = "Italia";
        city = "Napoli";
        String build = itineraryViewer.buildLocationString(state, region, city);
        assert(build.equals("Napoli, Italia"));
    }

    @Test
    public void buildLocationStringNoState() {
        city = "Napoli";
        region = "Campania";
        String build = itineraryViewer.buildLocationString(state, region, city);
        assert(build.equals("Napoli, Campania"));
    }

    @Test
    public void buildLocationStringState() {
        state = "Italia";
        String build = itineraryViewer.buildLocationString(state, region, city);
        assert(build.equals("Italia"));
    }

    @Test
    public void buildLocationStringRegion() {
        region = "Campania";
        String build = itineraryViewer.buildLocationString(state, region, city);
        assert(build.equals("Campania"));
    }

    @Test
    public void buildLocationStringCity() {
        city = "Napoli";
        String build = itineraryViewer.buildLocationString(state, region, city);
        assert(build.equals("Napoli"));
    }


    @Test (expected = NullPointerException.class)
    public void buildLocationStringNullStrings() {
        String build = itineraryViewer.buildLocationString(state, region, city);
        fail();
    }

    @Test
    public void buildLocationStringEmptyStrings() {
        state = "";
        region = "";
        city = "";
        String build = itineraryViewer.buildLocationString(state, region, city);
        assert(build.equals(""));
    }

    @Test
    public void buildLocationStringEmptyCity() {
        state = "Italia";
        region = "Campania";
        city = "";
        String build = itineraryViewer.buildLocationString(state, region, city);
        assert(build.equals("Campania, Italia"));
    }

    @Test
    public void buildLocationStringEmptyRegion() {
        state = "Italia";
        region = "";
        city = "Napoli";
        String build = itineraryViewer.buildLocationString(state, region, city);
        assert(build.equals("Napoli, Italia"));
    }

    @Test
    public void buildLocationStringEmptyState() {
        state = "";
        region = "Campania";
        city = "Napoli";
        String build = itineraryViewer.buildLocationString(state, region, city);
        assert(build.equals("Napoli, Campania"));
    }

    @Test
    public void buildLocationStringEmptyStateRegion() {
        state = "";
        region = "";
        city = "Napoli";
        String build = itineraryViewer.buildLocationString(state, region, city);
        assert(build.equals("Napoli"));
    }

    @Test
    public void buildLocationStringEmptyCityRegion() {
        state = "Italia";
        region = "";
        city = "";
        String build = itineraryViewer.buildLocationString(state, region, city);
        assert(build.equals("Italia"));
    }

    @Test
    public void buildLocationStringEmptyStateCity() {
        state = "";
        region = "Campania";
        city = "";
        String build = itineraryViewer.buildLocationString(state, region, city);
        assert(build.equals("Campania"));
    }


}