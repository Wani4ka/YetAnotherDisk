package me.wani4ka.yadisk;

import me.wani4ka.yadisk.exceptions.InvalidImportException;
import me.wani4ka.yadisk.exceptions.ItemNotFoundException;
import me.wani4ka.yadisk.models.ApiResult;
import me.wani4ka.yadisk.models.Item;
import me.wani4ka.yadisk.models.ItemHistoryUnit;
import me.wani4ka.yadisk.models.ItemImport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class ItemController {

    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private ItemHistoryRepository itemHistoryRepository;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    @GetMapping("/nodes/{id}")
    public Item getNode(@PathVariable String id) throws ItemNotFoundException {
        return itemRepository.findById(id).orElseThrow(ItemNotFoundException::new);
    }

    @PostMapping("/imports")
    public ApiResult importsPost(@Valid @RequestBody ItemImport.Request body) throws InvalidImportException {
        Set<Item> toSave = new HashSet<>();
        List<Item> toFindParent = new ArrayList<>();
        for (ItemImport itemImport : body.getItems()) {
            if (!itemImport.isValid())
                throw new InvalidImportException();
            Optional<Item> existing = itemRepository.findById(itemImport.getId());
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
        toFindParent.forEach(item -> toSave.addAll(item.findParent(itemRepository, local)));
        itemRepository.saveAll(toSave);
        itemHistoryRepository.saveAll(toSave.stream().map(Item::toHistoryUnit).collect(Collectors.toList()));
        return ApiResult.OK;
    }

    @DeleteMapping("/delete/{id}")
    public ApiResult delete(@PathVariable String id) throws ItemNotFoundException {
        if (!itemRepository.existsById(id))
            throw new ItemNotFoundException();
        Item item = itemRepository.findById(id).orElse(null);
        if (item != null && item.getParentObject() != null) {
            item.unparent();
            itemRepository.save(item.getParentObject());
        }
        itemRepository.deleteById(id);
        return ApiResult.OK;
    }

    @GetMapping("/updates")
    public ItemHistoryUnit.Response getUpdates() {
        Date from = Date.from(Instant.now().minus(24, ChronoUnit.HOURS));
        return new ItemHistoryUnit.Response(itemRepository.findItemsByDateAfter(from).stream()
                .map(Item::toHistoryUnit).toArray(ItemHistoryUnit[]::new));
    }

    @GetMapping("/node/{id}/history")
    public ItemHistoryUnit.Response getHistory(@PathVariable String id, @RequestParam(required = false) String dateStart, @RequestParam(required = false) String dateEnd) throws ParseException, ItemNotFoundException {
        Optional<Item> item = itemRepository.findById(id);
        if (item.isEmpty())
            throw new ItemNotFoundException();
        Date to = dateEnd == null ? new Date() : sdf.parse(dateEnd);
        List<ItemHistoryUnit> history = dateStart == null ?
                itemHistoryRepository.findItemHistoryUnitsByItemAndDateBefore(item.get(), to) :
                itemHistoryRepository.findItemHistoryUnitsByItemAndDateBetween(item.get(), sdf.parse(dateStart), to);
        history.forEach(unit -> System.out.println(unit.getId() + " " + unit.getDate()));
       return new ItemHistoryUnit.Response(history.toArray(ItemHistoryUnit[]::new));
    }
}
