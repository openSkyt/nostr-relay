package org.openskyt.nostrrelay.repository;

import lombok.NonNull;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

public class MongoConfig extends AbstractMongoClientConfiguration {
    @Override
    @NonNull
    protected String getDatabaseName() {
        return "test";
    }
}
