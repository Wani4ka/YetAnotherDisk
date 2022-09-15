package me.wani4ka.yadisk.services;

import me.wani4ka.yadisk.exceptions.ItemNotFoundException;
import me.wani4ka.yadisk.models.Item;
import me.wani4ka.yadisk.models.ItemHistoryUnit;
import me.wani4ka.yadisk.repositories.ItemHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Date;

@Service
public class ItemHistoryUnitService {

    @Autowired
    private ItemHistoryRepository repository;

    public void addHistoryUnits(Collection<Item> items) {
        repository.saveAll(items.stream().map(ItemHistoryUnit::new).toList());
    }

    public ItemHistoryUnit[] getHistory(Item item, Date from, Date to) throws ItemNotFoundException {
        if (item == null)
            throw new ItemNotFoundException();
        return (from == null ?
                repository.findItemHistoryUnitsByItemAndDateBefore(item, to) :
                repository.findItemHistoryUnitsByItemAndDateBetween(item, from, to)).toArray(ItemHistoryUnit[]::new);
    }

}
