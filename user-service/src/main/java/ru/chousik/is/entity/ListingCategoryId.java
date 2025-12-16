package ru.chousik.is.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Embeddable
public class ListingCategoryId implements Serializable {
    private static final long serialVersionUID = -7695586801180907140L;
    @NotNull
    @Column(name = "listing_id", nullable = false)
    private UUID listingId;

    @NotNull
    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ListingCategoryId entity = (ListingCategoryId) o;
        return Objects.equals(this.listingId, entity.listingId) &&
                Objects.equals(this.categoryId, entity.categoryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(listingId, categoryId);
    }

}