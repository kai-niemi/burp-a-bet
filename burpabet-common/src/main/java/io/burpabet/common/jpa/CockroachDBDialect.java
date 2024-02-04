package io.burpabet.common.jpa;

import org.hibernate.dialect.CockroachDialect;
import org.hibernate.dialect.identity.CockroachDBIdentityColumnSupport;
import org.hibernate.dialect.identity.IdentityColumnSupport;

public class CockroachDBDialect extends CockroachDialect {
    @Override
    public IdentityColumnSupport getIdentityColumnSupport() {
        return new CockroachDBIdentityColumnSupport() {
            @Override
            public String getIdentityInsertString() {
                return "unordered_unique_rowid()";
            }
        };
    }
}
