package com.shortener.gatling;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class ShortenerSimulation extends Simulation {

    // Base URL of your Spring Boot service
    private static final String BASE_URL = "http://localhost:8080";

    // JSON for the POST /api/shorten endpoint
    private static final String REQUEST_BODY = """
        {
            "url": "https://example.com/very/long/path"
        }
        """;

    // Define HTTP Protocol
    HttpProtocolBuilder httpProtocol = http
            .baseUrl(BASE_URL)
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    // POST scenario: shorten URL
    ScenarioBuilder shortenScenario = scenario("Shorten URL Scenario")
            .exec(
                    http("Create Short URL")
                            .post("/api/shorten")
                            .body(StringBody(REQUEST_BODY)).asJson()
                            .check(jsonPath("$.code").saveAs("shortCode"))
            );

    // GET scenario: redirect using saved code
    ScenarioBuilder redirectScenario = scenario("Redirect URL Scenario")
            .exec(
                    http("Create Short URL")
                            .post("/api/shorten")
                            .body(StringBody(REQUEST_BODY)).asJson()
                            .check(jsonPath("$.code").saveAs("shortCode"))
            )
            .pause(1)
            .exec(
                    http("Redirect")
                            .get(session -> "/" + session.getString("shortCode"))
                            .check(status().is(301))
            );

    {
        setUp(
                shortenScenario.injectOpen(rampUsers(50).during(10)),
                redirectScenario.injectOpen(rampUsers(50).during(10))
        ).protocols(httpProtocol);
    }
}
