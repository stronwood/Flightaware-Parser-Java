package com.slyecraft.flightawareparserjava;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class FlightAwareParser {
    private final String urlBase = "https://www.flightaware.com/live/";
    private String flightNum;
    private String aircraftType;
    private String flightOrg;
    private String flightDest;
    private String flightStatus;
    private String estGateDep;
    private String estTakeoff;
    private String estLand;
    private String estGateArriv;
    private String actGateDep;
    private String actTakeoff;
    private String actLand;
    private String actGateArriv;
    private String aircraftPosition;
    private String alt;
    private String gspd;
    private ZoneId zoneId;

    private String getTime(long epochTime) {
        if (epochTime != 0) {
            LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(epochTime), this.zoneId);
            return dateTime.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM));
        } else {
            return "Can't get time";
        }
    }

    public void getZoneId() {
        this.zoneId = ZoneId.systemDefault();
    }

    private Document fetchWebsiteContent(String flightno) throws IOException {

        URL url = new URL(urlBase + "flight/" + flightno);
        System.out.println("Attempting for site: " + url);
        return Jsoup.connect(url.toString()).get();

    }

    private JsonNode extractFlightData(Document doc) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectWriter objectWriter = objectMapper.writer(new DefaultPrettyPrinter());
        String scriptTagContent = doc.select("script:containsData(isLatLon)").html();
        int faidx1 = scriptTagContent.indexOf("trackpollBootstrap = ");
        String facontent = scriptTagContent.substring(faidx1 + 21);
        int faidx2 = facontent.length();
        facontent = facontent.substring(0, faidx2);
        facontent = facontent.replace(";</script>", "").trim();
        JsonNode rootNode = objectMapper.readTree(facontent);
        objectWriter.writeValue(new File("data.json"), rootNode);
        JsonNode flightsNode = rootNode.get("flights");
        String flightKey = flightsNode.fieldNames().next();
        return flightsNode.get(flightKey);

    }

    @JsonAnyGetter
    public String[] flightData(String flightcode) {

        try {
            Document websiteContent = fetchWebsiteContent(flightcode);
            JsonNode flightDetailsNode = extractFlightData(websiteContent);
            String flightKey = flightDetailsNode.fieldNames().next();

            aircraftType = flightDetailsNode.get("aircraft").get("friendlyType").asText();
            if (flightDetailsNode.get("friendlyIdent") != null) {
                flightNum = flightDetailsNode.get("friendlyIdent").asText();
            } else {
                flightNum = "is not currently flying";
            }

            flightStatus = flightDetailsNode.get("flightStatus").asText();
            actGateDep = getTime(flightDetailsNode.get("gateDepartureTimes").get("actual").asLong());
            actTakeoff = getTime(flightDetailsNode.get("takeoffTimes").get("actual").asLong());
            actLand = getTime(flightDetailsNode.get("landingTimes").get("actual").asLong());
            actGateArriv = getTime(flightDetailsNode.get("gateArrivalTimes").get("actual").asLong());
            flightOrg = flightDetailsNode.get("origin").get("friendlyLocation").asText();
            flightDest = flightDetailsNode.get("destination").get("friendlyLocation").asText();
            estGateDep = getTime(flightDetailsNode.get("gateDepartureTimes").get("estimated").asLong());
            estTakeoff = getTime(flightDetailsNode.get("takeoffTimes").get("estimated").asLong());
            estLand = getTime(flightDetailsNode.get("landingTimes").get("estimated").asLong());
            estGateArriv = getTime(flightDetailsNode.get("gateArrivalTimes").get("estimated").asLong());
            alt = flightDetailsNode.get("altitude").asText();
            gspd = flightDetailsNode.get("groundspeed").asText();

            if (flightStatus.equals("airborne")) {

                if (actGateDep == null) {
                    aircraftPosition = "Unknown";
                } else if (actGateDep != null && actTakeoff == null) {
                    aircraftPosition = "Departed gate. Taxiing for takeoff";
                } else if (actGateDep != null && actTakeoff != null) {
                    JsonNode distanceNode = flightDetailsNode.get("distance");
                    int distcov = distanceNode.get("elapsed").asInt();
                    int distrem = distanceNode.get("remaining").asInt();
                    aircraftPosition = "In air, covered " + distcov + " nautical miles with " + distrem + " nautical miles remaining.";
                }
            } else if (flightStatus.equals("arrived")) {

                if (actLand == null) {
                    aircraftPosition = "Unknown";
                } else if (actLand != null && actGateArriv == null) {
                    aircraftPosition = "Arrived at destination. Taxiing to gate";
                } else if (actLand != null && actGateArriv != null) {
                    aircraftPosition = "Arrived at destination and at the gate";
                }
            } else {
                aircraftPosition = "Unknown";
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String[]{
                flightNum, aircraftType, flightOrg, flightDest, flightStatus, alt, gspd, estGateDep, estTakeoff, estLand,
                estGateArriv, actGateDep, actTakeoff, actLand, actGateArriv, aircraftPosition
        };
    }
}
