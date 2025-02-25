package org.zalando.riptide.failsafe;

import dev.failsafe.function.ContextualSupplier;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import dev.failsafe.ExecutionContext;
import org.apiguardian.api.API;
import org.springframework.http.client.ClientHttpResponse;
import org.zalando.riptide.HttpResponseException;

import java.time.Clock;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static lombok.AccessLevel.PRIVATE;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;

/**
 * @see <a href="https://tools.ietf.org/html/rfc7231#section-7.1.3">RFC 7231, section 7.1.3: Retry-After</a>
 */
@API(status = EXPERIMENTAL)
@Slf4j
@AllArgsConstructor(access = PRIVATE)
public final class RetryAfterDelayFunction implements ContextualSupplier<ClientHttpResponse, Duration> {

    private final DelayParser parser;

    public RetryAfterDelayFunction(final Clock clock) {
        this(new CompositeDelayParser(Arrays.asList(
                new SecondsDelayParser(),
                new HttpDateDelayParser(clock)
        )));
    }

    @Override
    public Duration get(final ExecutionContext<ClientHttpResponse> context) {
        return ofNullable(context)
                .map(ExecutionContext::getLastException)
                .filter(HttpResponseException.class::isInstance)
                .map(HttpResponseException.class::cast)
                .map(response -> response.getResponseHeaders().getFirst("Retry-After"))
                .map(parser::parse)
                .orElse(Duration.ofMinutes(-1));
    }

}
