/**
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.watsonhealth.fhir.model.type;

import java.util.Collection;
import java.util.Objects;

public class PaymentReconciliationStatus extends Code {
    /**
     * Active
     */
    public static final PaymentReconciliationStatus ACTIVE = PaymentReconciliationStatus.of(ValueSet.ACTIVE);

    /**
     * Cancelled
     */
    public static final PaymentReconciliationStatus CANCELLED = PaymentReconciliationStatus.of(ValueSet.CANCELLED);

    /**
     * Draft
     */
    public static final PaymentReconciliationStatus DRAFT = PaymentReconciliationStatus.of(ValueSet.DRAFT);

    /**
     * Entered in Error
     */
    public static final PaymentReconciliationStatus ENTERED_IN_ERROR = PaymentReconciliationStatus.of(ValueSet.ENTERED_IN_ERROR);

    private volatile int hashCode;

    private PaymentReconciliationStatus(Builder builder) {
        super(builder);
    }

    public static PaymentReconciliationStatus of(java.lang.String value) {
        return PaymentReconciliationStatus.builder().value(value).build();
    }

    public static PaymentReconciliationStatus of(ValueSet value) {
        return PaymentReconciliationStatus.builder().value(value).build();
    }

    public static String string(java.lang.String value) {
        return PaymentReconciliationStatus.builder().value(value).build();
    }

    public static Code code(java.lang.String value) {
        return PaymentReconciliationStatus.builder().value(value).build();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PaymentReconciliationStatus other = (PaymentReconciliationStatus) obj;
        return Objects.equals(id, other.id) && Objects.equals(extension, other.extension) && Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        int result = hashCode;
        if (result == 0) {
            result = Objects.hash(id, extension, value);
            hashCode = result;
        }
        return result;
    }
    public Builder toBuilder() {
        Builder builder = new Builder();
        builder.id = id;
        builder.extension.addAll(extension);
        builder.value = value;
        return builder;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends Code.Builder {
        private Builder() {
            super();
        }

        @Override
        public Builder id(java.lang.String id) {
            return (Builder) super.id(id);
        }

        @Override
        public Builder extension(Extension... extension) {
            return (Builder) super.extension(extension);
        }

        @Override
        public Builder extension(Collection<Extension> extension) {
            return (Builder) super.extension(extension);
        }

        @Override
        public Builder value(java.lang.String value) {
            return (Builder) super.value(ValueSet.from(value).value());
        }

        public Builder value(ValueSet value) {
            return (Builder) super.value(value.value());
        }

        @Override
        public PaymentReconciliationStatus build() {
            return new PaymentReconciliationStatus(this);
        }
    }

    public enum ValueSet {
        /**
         * Active
         */
        ACTIVE("active"),

        /**
         * Cancelled
         */
        CANCELLED("cancelled"),

        /**
         * Draft
         */
        DRAFT("draft"),

        /**
         * Entered in Error
         */
        ENTERED_IN_ERROR("entered-in-error");

        private final java.lang.String value;

        ValueSet(java.lang.String value) {
            this.value = value;
        }

        public java.lang.String value() {
            return value;
        }

        public static ValueSet from(java.lang.String value) {
            for (ValueSet c : ValueSet.values()) {
                if (c.value.equals(value)) {
                    return c;
                }
            }
            throw new IllegalArgumentException(value);
        }
    }
}
