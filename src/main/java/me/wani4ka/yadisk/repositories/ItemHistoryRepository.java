package me.wani4ka.yadisk.repositories;

import me.wani4ka.yadisk.models.Item;
import me.wani4ka.yadisk.models.ItemHistoryUnit;
import org.springframework.data.repository.CrudRepository;

import java.util.Date;
import java.util.List;

public interface ItemHistoryRepository extends CrudRepository<ItemHistoryUnit, String> {
    List<ItemHistoryUnit> findItemHistoryUnitsByItemAndDateBefore(Item item, Date date);
    List<ItemHistoryUnit> findItemHistoryUnitsByDateBetween(Date from, Date to);
    List<ItemHistoryUnit> findItemHistoryUnitsByItemAndDateBetween(Item item, Date from, Date to);
}
