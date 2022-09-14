package me.wani4ka.yadisk.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import me.wani4ka.yadisk.ItemRepository;
import me.wani4ka.yadisk.exceptions.InvalidImportException;

import javax.persistence.*;
import java.io.IOException;
import java.util.*;

@Entity
@JsonSerialize(using = Item.Serializer.class)
public class Item {
    private ItemType type;
    private String url;
    @Id
    private String id;
    private String parentId;
    @JsonIgnore
    @ManyToOne
    private Item parentObject;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private int size;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @OneToMany(mappedBy = "parentObject", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Item> children;

    protected Item() {}

    public Item(ItemImport sysItemImport) {
        this.id = sysItemImport.getId();
        this.url = sysItemImport.getUrl();
        this.parentId = sysItemImport.getParentId();
        this.type = sysItemImport.getType();
        this.children = this.type == ItemType.FOLDER ? new HashSet<>() : null;
        this.size = this.type == ItemType.FILE ? sysItemImport.getSize() : 0;
    }

    public ItemType getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    public String getId() {
        return id;
    }

    public int getSize() {
        return size;
    }

    public String getParentId() {
        return parentId;
    }

    public Collection<Item> getChildren() {
        return children != null ? Collections.unmodifiableCollection(children) : null;
    }

    public Item getParentObject() {
        return parentObject;
    }

    public Collection<Item> update(ItemImport itemImport) throws InvalidImportException {
        ItemType type = getType();
        if (type != itemImport.getType()) throw new InvalidImportException();
        if ((type == ItemType.FOLDER) != (itemImport.getUrl() == null))
            throw new InvalidImportException();
        if (type == ItemType.FILE && itemImport.getUrl().length() > 255)
            throw new InvalidImportException();
        if (type == ItemType.FOLDER && itemImport.getSize() != null)
            throw new InvalidImportException();
        if (type == ItemType.FILE && itemImport.getSize() <= 0)
            throw new InvalidImportException();

        List<Item> result = new ArrayList<>();
        result.add(this);
        setUrl(itemImport.getUrl());
        setParentId(itemImport.getParentId());

        Item parent = getParentObject();
        if (parent != null) {
            parent.removeChild(this);
            result.add(parent);
            setParentObject(null); // need to call findParent after a while
        }
        if (type == ItemType.FILE)
            setSize(itemImport.getSize());

        return result;
    }

    public Collection<Item> findParent(ItemRepository repo, Map<String, Item> local) {
        List<Item> result = new ArrayList<>();
        if (parentId == null)
            return result;
        if (parentObject != null) {
            parentObject.removeChild(this);
            result.add(parentObject);
        }
        Item parent = local.getOrDefault(parentId, repo.findById(parentId).orElse(null));
        if (parent != null) {
            setParentObject(parent);
            parent.addChild(this);
            result.add(parent);
        }
        result.add(this);
        return result;
    }

    public void unparent() {
        setParentId(null);
        if (parentObject != null)
            parentObject.removeChild(this);
    }

    void setParentObject(Item parent) {
        this.parentObject = parent;
    }

    void addChild(Item child) {
        if (getType() != ItemType.FOLDER)
            return;
        child.setParentObject(this);
        if (children.add(child))
            changeSize(child.getSize());
    }

    void removeChild(Item child) {
        if (getType() != ItemType.FOLDER)
            return;
        if (children.remove(child))
            changeSize(-child.getSize());
    }

    private void changeSize(int delta) {
        size = size + delta;
        System.out.println("Change size of " + id + ": " + size);
        if (parentObject != null)
            parentObject.changeSize(delta);
    }

    protected void setType(ItemType type) {
        this.type = type;
    }

    protected void setUrl(String url) {
        this.url = url;
    }

    protected void setId(String id) {
        this.id = id;
    }

    protected void setParentId(String parentId) {
        this.parentId = parentId;
    }

    protected void setSize(int size) {
        this.size = size;
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
        if (o == null || getClass() != o.getClass()) return false;
        Item that = (Item) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return id;
    }

    public static class Serializer extends StdSerializer<Item> {

        public Serializer() {
            this(null);
        }

        public Serializer(Class<Item> t) {
            super(t);
        }

        @Override
        public void serialize(Item value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeStartObject();
            gen.writeStringField("id", value.id);
            gen.writeStringField("url", value.url);
            gen.writeStringField("parentId", value.parentId);
            gen.writeStringField("type", value.type.name());
            gen.writeNumberField("size", value.size);
            if (value.type == ItemType.FOLDER) {
                gen.writeArrayFieldStart("children");
                for (Item child : value.children)
                    gen.writeObject(child);
                gen.writeEndArray();
            }
            gen.writeEndObject();
        }
    }
}
