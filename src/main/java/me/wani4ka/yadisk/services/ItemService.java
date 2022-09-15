package me.wani4ka.yadisk.services;

import me.wani4ka.yadisk.exceptions.ValidationFailedException;
import me.wani4ka.yadisk.exceptions.ItemNotFoundException;
import me.wani4ka.yadisk.models.Item;
import me.wani4ka.yadisk.models.ItemHistoryUnit;
import me.wani4ka.yadisk.models.ItemImport;
import me.wani4ka.yadisk.repositories.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ItemService {

    @Autowired
    private ItemRepository repository;

    @Autowired
    private ItemHistoryUnitService historyUnitsService;

    public Item getItem(String id) throws ItemNotFoundException {
        return repository.findById(id).orElseThrow(ItemNotFoundException::new);
    }

    public void importItems(ItemImport[] itemImports) throws ValidationFailedException {
        Set<Item> toSave = new HashSet<>();
        List<Item> toFindParent = new ArrayList<>();
        for (ItemImport itemImport : itemImports) {
            if (!itemImport.isValid())
                throw new ValidationFailedException();
            Optional<Item> existing = repository.findById(itemImport.getId());
            Item item;
            if (existing.isPresent()) {
                toSave.addAll(existing.get().update(itemImport));
                item = existing.get();
            } else {
                item = new Item(itemImport);
                toSave.add(item);
            }
            toFindParent.add(item);
        }
        Map<String, Item> local = toSave.stream().collect(Collectors.toMap(Item::getId, known -> known));
        toFindParent.forEach(item -> toSave.addAll(findParentForItem(item, local)));
        repository.saveAll(toSave);
        historyUnitsService.addHistoryUnits(toSave);
    }

    public void deleteItem(Item item) throws ItemNotFoundException {
        if (item == null)
            throw new ItemNotFoundException();
        if (item.getParentObject() != null) {
            item.unparent();
            repository.save(item.getParentObject());
        }
        repository.delete(item);
    }

    public ItemHistoryUnit[] findRecentlyChangedItems(Date from) {
        return repository.findItemsByDateAfter(from).stream().map(ItemHistoryUnit::new).toArray(ItemHistoryUnit[]::new);
    }

    public ItemHistoryUnit[] getChangesHistory(Item item, Date from, Date to) throws ItemNotFoundException {
        return historyUnitsService.getHistory(item, from, to);
    }

    protected List<Item> findParentForItem(Item item, Map<String, Item> local) {
        List<Item> result = new ArrayList<>();
        if (item.getParentId() == null)
            return result;
        if (item.getParentObject() != null) {
            result.add(item.getParentObject());
            item.getParentObject().removeChild(item);
        }
        Item parent = local.getOrDefault(item.getParentId(), repository.findById(item.getParentId()).orElse(null));
        if (parent != null) {
            item.setParentObject(parent);
            parent.addChild(item);
            result.add(parent);
        }
        result.add(item);
        return result;
    }

}
