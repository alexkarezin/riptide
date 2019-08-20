package org.zalando.riptide.opentracing;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import lombok.AllArgsConstructor;
import net.jodah.failsafe.function.ContextualSupplier;
import org.apiguardian.api.API;
import org.zalando.riptide.failsafe.TaskDecorator;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

@API(status = EXPERIMENTAL)
@AllArgsConstructor
public final class TracedTaskDecorator implements TaskDecorator {

    private final Tracer tracer;

    @Override
    public <T> ContextualSupplier<T> decorate(final ContextualSupplier<T> supplier) {
        final Span span = tracer.activeSpan();

        return context -> {
            try (final Scope ignored = tracer.activateSpan(span)) {
                return supplier.get(context);
            }
        };
    }
}
