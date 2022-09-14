package me.wani4ka.yadisk.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import me.wani4ka.yadisk.ItemRepository;
import me.wani4ka.yadisk.exceptions.InvalidImportException;
import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import java.util.*;

@Entity
public class Item {
    private ItemType type;
    private String url;
    @Id
    private String id;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private Date date;
    private String parentId;
    @JsonIgnore
    @ManyToOne
    private Item parentObject;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private int size;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @OneToMany(mappedBy = "parentObject", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Item> children;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Transient
    private Set<Item> nullableChildren;
    @JsonIgnore
    @OneToMany(mappedBy = "item", fetch = FetchType.LAZY, orphanRemoval = true)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE)
    private List<ItemHistoryUnit> historyUnits;

    protected Item() {}

    @PostLoad
    public void postLoad() {
        nullableChildren = getType() == ItemType.FOLDER ? children : null;
    }

    public Item(ItemImport sysItemImport) {
        this.id = sysItemImport.getId();
        this.url = sysItemImport.getUrl();
        this.parentId = sysItemImport.getParentId();
        this.date = new Date();
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

    public Date getDate() {
        return date;
    }

    public int getSize() {
        return size;
    }

    public String getParentId() {
        return parentId;
    }

    @JsonProperty("children")
    public Set<Item> getNullableChildren() {
        return nullableChildren;
    }

    protected List<ItemHistoryUnit> getHistoryUnits() {
        return historyUnits;
    }

    protected void setHistoryUnits(List<ItemHistoryUnit> historyUnits) {
        this.historyUnits = historyUnits;
    }

    public Collection<Item> getChildren() {
        return children != null ? Collections.unmodifiableCollection(children) : null;
    }

    public Item getParentObject() {
        return parentObject;
    }

    public ItemHistoryUnit toHistoryUnit() {
        return new ItemHistoryUnit(this);
    }

    public boolean isValidImport(ItemImport itemImport) {
        return getType() == itemImport.getType() && itemImport.isValid();
    }

    public Collection<Item> update(ItemImport itemImport) throws InvalidImportException {
        if (!isValidImport(itemImport))
            throw new InvalidImportException();

        List<Item> result = new ArrayList<>();
        result.add(this);
        setUrl(itemImport.getUrl());
        setParentId(itemImport.getParentId());
        setDate(new Date());

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
        if (children.add(child)) {
            changeSize(child.getSize());
            setDate(new Date());
        }
    }

    void removeChild(Item child) {
        if (getType() != ItemType.FOLDER)
            return;
        if (children.remove(child)) {
            changeSize(-child.getSize());
            setDate(new Date());
        }
    }

    private void changeSize(int delta) {
        size = size + delta;
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

    protected void setDate(Date date) {
        this.date = date;
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
}
