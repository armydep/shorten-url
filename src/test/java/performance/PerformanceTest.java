package performance;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class PerformanceTest extends Simulation {

    private static final AtomicInteger counter = new AtomicInteger(1);
    private static final Integer total_users = 1000;

    private static Iterator<Map<String, Object>> feeder = new Iterator<>() {
        @Override
        public boolean hasNext() {
            return true; // Infinite feeder
        }

        @Override
        public Map<String, Object> next() {
            return Collections.singletonMap("counter", counter.getAndIncrement());
        }
    };

    HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8080") // Change to your actual base URL
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    ScenarioBuilder scn = scenario("Shorten and Follow Redirect")
            .feed(feeder)
            .exec(http("POST shorten")
                    .post("/api/shorten")
                    .body(StringBody("{ \"url\": \"https://example.com/very/long/path/#{counter}\" }"))
                    .asJson()
                    .check(status().is(200))
                    .check(jsonPath("$.code").saveAs("shortCode"))
            )
            .exec(session -> {
                //System.out.println("Short code received: " + session.get("shortCode"));
                return session;
            })
            /*.pause(Duration.ofMillis(10))*/
            .exec(http("Redirect")
                    .get("/#{shortCode}")
                    .disableFollowRedirect()
                    .check(status().saveAs("infoStatus"))
                    .check(status().is(301))
            )
            .exec(session -> {
                //System.out.println("GET /api/m3 => status: " + session.getInt("infoStatus"));
                return session;
            })
            /*.pause(Duration.ofMillis(10))*/
            .exec(http("GET clicks count stats")
                    .get("/api/stats/#{shortCode}")
                    .check(status().in(200))
            );

    {
        setUp(scn.injectOpen(rampUsers(total_users).during(Duration.ofSeconds(60)))).protocols(httpProtocol);
    }
}