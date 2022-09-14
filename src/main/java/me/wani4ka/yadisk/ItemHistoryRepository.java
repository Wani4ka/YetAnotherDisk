package me.wani4ka.yadisk;

import me.wani4ka.yadisk.models.Item;
import me.wani4ka.yadisk.models.ItemHistoryUnit;
import org.springframework.data.repository.CrudRepository;

import java.util.Date;
import java.util.List;

public interface ItemHistoryRepository extends CrudRepository<ItemHistoryUnit, String> {
    List<ItemHistoryUnit> findItemHistoryUnitsByItemAndDateBefore(Item item, Date date);
    List<ItemHistoryUnit> findItemHistoryUnitsByItemAndDateBetween(Item item, Date date, Date date2);
}
