package io.sinapsi.hive.validation;

import java.util.List;
import java.util.ServiceLoader;

public final class ValidationProviders {

    private static final ValidationProvider PROVIDER = loadProvider();

    private ValidationProviders() {}

    public static ValidationProvider get() {
        return PROVIDER;
    }

    private static ValidationProvider loadProvider() {

        List<ValidationProvider> providers =
            ServiceLoader.load(ValidationProvider.class)
                .stream()
                .map(ServiceLoader.Provider::get)
                .toList();

        if (providers.isEmpty()) {
            return NoopValidationProvider.INSTANCE;
        }

        if (providers.size() > 1) {
            throw new IllegalStateException(
                "Multiple ValidationProvider implementations found: "
                    + providers
            );
        }

        return providers.getFirst();
    }

}