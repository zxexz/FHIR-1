/**
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.watsonhealth.fhir.model.type;

import java.util.Collection;
import java.util.Objects;

import javax.annotation.Generated;

import com.ibm.watsonhealth.fhir.model.annotation.Constraint;
import com.ibm.watsonhealth.fhir.model.type.ContactPointSystem;
import com.ibm.watsonhealth.fhir.model.type.ContactPointUse;
import com.ibm.watsonhealth.fhir.model.visitor.Visitor;

/**
 * <p>
 * Details for all kinds of technology mediated contact points for a person or organization, including telephone, email, 
 * etc.
 * </p>
 */
@Constraint(
    id = "cpt-2",
    level = "Rule",
    location = "(base)",
    description = "A system is required if a value is provided.",
    expression = "value.empty() or system.exists()"
)
@Generated("com.ibm.watsonhealth.fhir.tools.CodeGenerator")
public class ContactPoint extends Element {
    private final ContactPointSystem system;
    private final String value;
    private final ContactPointUse use;
    private final PositiveInt rank;
    private final Period period;

    private volatile int hashCode;

    private ContactPoint(Builder builder) {
        super(builder);
        system = builder.system;
        value = builder.value;
        use = builder.use;
        rank = builder.rank;
        period = builder.period;
    }

    /**
     * <p>
     * Telecommunications form for contact point - what communications system is required to make use of the contact.
     * </p>
     * 
     * @return
     *     An immutable object of type {@link ContactPointSystem}.
     */
    public ContactPointSystem getSystem() {
        return system;
    }

    /**
     * <p>
     * The actual contact point details, in a form that is meaningful to the designated communication system (i.e. phone 
     * number or email address).
     * </p>
     * 
     * @return
     *     An immutable object of type {@link String}.
     */
    public String getValue() {
        return value;
    }

    /**
     * <p>
     * Identifies the purpose for the contact point.
     * </p>
     * 
     * @return
     *     An immutable object of type {@link ContactPointUse}.
     */
    public ContactPointUse getUse() {
        return use;
    }

    /**
     * <p>
     * Specifies a preferred order in which to use a set of contacts. ContactPoints with lower rank values are more preferred 
     * than those with higher rank values.
     * </p>
     * 
     * @return
     *     An immutable object of type {@link PositiveInt}.
     */
    public PositiveInt getRank() {
        return rank;
    }

    /**
     * <p>
     * Time period when the contact point was/is in use.
     * </p>
     * 
     * @return
     *     An immutable object of type {@link Period}.
     */
    public Period getPeriod() {
        return period;
    }

    @Override
    public void accept(java.lang.String elementName, Visitor visitor) {
        if (visitor.preVisit(this)) {
            visitor.visitStart(elementName, this);
            if (visitor.visit(elementName, this)) {
                // visit children
                accept(id, "id", visitor);
                accept(extension, "extension", visitor, Extension.class);
                accept(system, "system", visitor);
                accept(value, "value", visitor);
                accept(use, "use", visitor);
                accept(rank, "rank", visitor);
                accept(period, "period", visitor);
            }
            visitor.visitEnd(elementName, this);
            visitor.postVisit(this);
        }
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
        ContactPoint other = (ContactPoint) obj;
        return Objects.equals(id, other.id) && 
            Objects.equals(extension, other.extension) && 
            Objects.equals(system, other.system) && 
            Objects.equals(value, other.value) && 
            Objects.equals(use, other.use) && 
            Objects.equals(rank, other.rank) && 
            Objects.equals(period, other.period);
    }

    @Override
    public int hashCode() {
        int result = hashCode;
        if (result == 0) {
            result = Objects.hash(id, 
                extension, 
                system, 
                value, 
                use, 
                rank, 
                period);
            hashCode = result;
        }
        return result;
    }

    @Override
    public Builder toBuilder() {
        return new Builder().from(this);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends Element.Builder {
        // optional
        private ContactPointSystem system;
        private String value;
        private ContactPointUse use;
        private PositiveInt rank;
        private Period period;

        private Builder() {
            super();
        }

        /**
         * <p>
         * Unique id for the element within a resource (for internal references). This may be any string value that does not 
         * contain spaces.
         * </p>
         * 
         * @param id
         *     Unique id for inter-element referencing
         * 
         * @return
         *     A reference to this Builder instance.
         */
        @Override
        public Builder id(java.lang.String id) {
            return (Builder) super.id(id);
        }

        /**
         * <p>
         * May be used to represent additional information that is not part of the basic definition of the element. To make the 
         * use of extensions safe and manageable, there is a strict set of governance applied to the definition and use of 
         * extensions. Though any implementer can define an extension, there is a set of requirements that SHALL be met as part 
         * of the definition of the extension.
         * </p>
         * 
         * @param extension
         *     Additional content defined by implementations
         * 
         * @return
         *     A reference to this Builder instance.
         */
        @Override
        public Builder extension(Extension... extension) {
            return (Builder) super.extension(extension);
        }

        /**
         * <p>
         * May be used to represent additional information that is not part of the basic definition of the element. To make the 
         * use of extensions safe and manageable, there is a strict set of governance applied to the definition and use of 
         * extensions. Though any implementer can define an extension, there is a set of requirements that SHALL be met as part 
         * of the definition of the extension.
         * </p>
         * 
         * @param extension
         *     Additional content defined by implementations
         * 
         * @return
         *     A reference to this Builder instance.
         */
        @Override
        public Builder extension(Collection<Extension> extension) {
            return (Builder) super.extension(extension);
        }

        /**
         * <p>
         * Telecommunications form for contact point - what communications system is required to make use of the contact.
         * </p>
         * 
         * @param system
         *     phone | fax | email | pager | url | sms | other
         * 
         * @return
         *     A reference to this Builder instance.
         */
        public Builder system(ContactPointSystem system) {
            this.system = system;
            return this;
        }

        /**
         * <p>
         * The actual contact point details, in a form that is meaningful to the designated communication system (i.e. phone 
         * number or email address).
         * </p>
         * 
         * @param value
         *     The actual contact point details
         * 
         * @return
         *     A reference to this Builder instance.
         */
        public Builder value(String value) {
            this.value = value;
            return this;
        }

        /**
         * <p>
         * Identifies the purpose for the contact point.
         * </p>
         * 
         * @param use
         *     home | work | temp | old | mobile - purpose of this contact point
         * 
         * @return
         *     A reference to this Builder instance.
         */
        public Builder use(ContactPointUse use) {
            this.use = use;
            return this;
        }

        /**
         * <p>
         * Specifies a preferred order in which to use a set of contacts. ContactPoints with lower rank values are more preferred 
         * than those with higher rank values.
         * </p>
         * 
         * @param rank
         *     Specify preferred order of use (1 = highest)
         * 
         * @return
         *     A reference to this Builder instance.
         */
        public Builder rank(PositiveInt rank) {
            this.rank = rank;
            return this;
        }

        /**
         * <p>
         * Time period when the contact point was/is in use.
         * </p>
         * 
         * @param period
         *     Time period when the contact point was/is in use
         * 
         * @return
         *     A reference to this Builder instance.
         */
        public Builder period(Period period) {
            this.period = period;
            return this;
        }

        @Override
        public ContactPoint build() {
            return new ContactPoint(this);
        }

        private Builder from(ContactPoint contactPoint) {
            id = contactPoint.id;
            extension.addAll(contactPoint.extension);
            system = contactPoint.system;
            value = contactPoint.value;
            use = contactPoint.use;
            rank = contactPoint.rank;
            period = contactPoint.period;
            return this;
        }
    }
}
