package me.wani4ka.yadisk;

import me.wani4ka.yadisk.exceptions.InvalidImportException;
import me.wani4ka.yadisk.exceptions.ItemNotFoundException;
import me.wani4ka.yadisk.models.ApiResult;
import me.wani4ka.yadisk.models.Item;
import me.wani4ka.yadisk.models.ItemImport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class ItemController {

    @Autowired
    private ItemRepository itemRepository;

    @GetMapping("/nodes/{id}")
    public Item getNode(@PathVariable String id) throws ItemNotFoundException {
        return itemRepository.findById(id).orElseThrow(ItemNotFoundException::new);
    }

    @PostMapping("/imports")
    public ApiResult importsPost(@Valid @RequestBody ItemImport.Request body) throws InvalidImportException {
        Set<Item> toSave = new HashSet<>();
        List<Item> toFindParent = new ArrayList<>();
        for (ItemImport itemImport : body.getItems()) {
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
        toSave.forEach(System.out::println);
        itemRepository.saveAll(toSave);
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
}
