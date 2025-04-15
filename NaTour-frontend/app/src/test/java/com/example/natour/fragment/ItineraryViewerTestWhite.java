package com.example.natour.fragment;

import static org.junit.Assert.*;

import com.example.natour.entity.Itinerary;

import org.junit.Before;
import org.junit.Test;

public class ItineraryViewerTestWhite {

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

    @Test (expected = NullPointerException.class)
    public void testGenerateBuildStringWhiteBoxPath_NI_1_2_NF() {
        String build = itineraryViewer.buildLocationString(state, region, city);
        fail();
    }

    @Test
    public void testGenerateBuildStringWhiteBoxPath_NI_1_3_4_5_6_8_9_10_11_12_14_15_16_17_18_NF() {
        state = "Italia";
        region = "Campania";
        city = "Napoli";
        String build = itineraryViewer.buildLocationString(state, region, city);
        assert(build.equals("Napoli, Campania, Italia"));
    }

    @Test
    public void testGenerateBuildStringWhiteBoxPath_NI_1_3_4_5_6_7_NF() {
        city = "Napoli";
        String build = itineraryViewer.buildLocationString(state, region, city);
        assert (build.equals("Napoli"));
    }

    @Test
    public void testGenerateBuildStringWhiteBoxPath_NI_1_3_4_10_11_12_13_NF() {
        region = "Campania";
        String build = itineraryViewer.buildLocationString(state, city, region);
        assert (build.equals("Campania"));
    }

    @Test
    public void testGenerateBuildStringWhiteBoxPath_NI_1_3_4_5_10_16_18_NF() {
        state = "Italia";
        String build = itineraryViewer.buildLocationString(state, region, city);
        assert (build.equals("Italia"));
    }

    @Test
    public void testGenerateBuildStringWhiteBoxPath_NI_1_3_4_5_6_8_10_16_18_NF() {
        city = "Napoli";
        region = "";
        state = "";
        String build = itineraryViewer.buildLocationString(state, region, city);
        assert (build.equals("Napoli"));
    }

    @Test
    public void testGenerateBuildStringWhiteBoxPath_NI_1_3_4_10_11_12_14_16_18_NF() {
        state = "";
        region = "Campania";
        String build = itineraryViewer.buildLocationString(state, region, city);
        assert (build.equals("Campania"));
    }

}