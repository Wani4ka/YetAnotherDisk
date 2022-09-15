package me.wani4ka.yadisk.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import me.wani4ka.yadisk.exceptions.ValidationFailedException;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import java.util.*;

@Entity
public class Item {
    @Getter @Setter(AccessLevel.PROTECTED)
    private ItemType type;
    @Getter @Setter(AccessLevel.PROTECTED)
    private String url;
    @Id
    @Getter @Setter(AccessLevel.PROTECTED)
    private String id;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    @Getter @Setter
    private Date date;
    @Getter @Setter(AccessLevel.PROTECTED)
    private String parentId;
    @JsonIgnore
    @ManyToOne
    @Getter @Setter
    private Item parentObject;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Getter @Setter(AccessLevel.PROTECTED)
    private int size;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @OneToMany(mappedBy = "parentObject", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Getter
    private Set<Item> children;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Transient
    @Getter(onMethod = @__(@JsonProperty("children")))
    private Set<Item> nullableChildren;
    @JsonIgnore
    @OneToMany(mappedBy = "item", fetch = FetchType.LAZY, orphanRemoval = true)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE)
    @Getter(AccessLevel.PROTECTED) @Setter(AccessLevel.PROTECTED)
    private List<ItemHistoryUnit> historyUnits;

    protected Item() {}

    @PostLoad
    public void postLoad() {
        nullableChildren = getType() == ItemType.FOLDER ? children : null;
    }

    private Item(ItemImport sysItemImport) {
        this.id = sysItemImport.getId();
        this.url = sysItemImport.getUrl();
        this.parentId = sysItemImport.getParentId();
        this.date = new Date();
        this.type = sysItemImport.getType();
        this.children = this.type == ItemType.FOLDER ? new HashSet<>() : null;
        this.size = this.type == ItemType.FILE ? sysItemImport.getSize() : 0;
    }

    public static Item fromImport(ItemImport itemImport, Date when) {
        Item result = new Item(itemImport);
        result.date = when;
        return result;
    }

    public boolean isValidImport(ItemImport itemImport) {
        return getType() == itemImport.getType() && itemImport.isValid();
    }

    public Collection<Item> update(ItemImport itemImport, Date when) throws ValidationFailedException {
        if (!isValidImport(itemImport))
            throw new ValidationFailedException();

        List<Item> result = new ArrayList<>();
        result.add(this);
        setUrl(itemImport.getUrl());
        setParentId(itemImport.getParentId());

        Item parent = getParentObject();
        if (parent != null) {
            parent.removeChild(this, when);
            result.add(parent);
            setParentObject(null); // need to call findParent after a while
        }
        if (type == ItemType.FILE)
            setSize(itemImport.getSize());
        updateDate(when);

        return result;
    }

    public void unparent(Date when) {
        setParentId(null);
        if (parentObject != null)
            parentObject.removeChild(this, when);
    }

    public void addChild(Item child) {
        if (getType() != ItemType.FOLDER)
            return;
        child.setParentObject(this);
        if (children.add(child)) {
            changeSize(child.getSize());
            updateDate(child.getDate());
        }
    }

    public void removeChild(Item child, Date when) {
        if (getType() != ItemType.FOLDER)
            return;
        if (children.remove(child)) {
            changeSize(-child.getSize());
            updateDate(when);
        }
    }

    public void updateDate(Date date) {
        if (this.date.before(date)) {
            this.date = date;
            if (parentObject != null)
                parentObject.updateDate(date);
        }
    }

    private void changeSize(int delta) {
        size = size + delta;
        if (parentObject != null)
            parentObject.changeSize(delta);
    }

    protected void setChildren(Set<Item> children) {
        this.children = children;
        if (getType() == ItemType.FOLDER) {
            setSize(0);
            for (Item child : children)
                changeSize(child.getSize());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Item item = (Item) o;
        return id != null && Objects.equals(id, item.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return id;
    }
}
