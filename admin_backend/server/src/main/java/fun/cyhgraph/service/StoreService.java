package fun.cyhgraph.service;

import fun.cyhgraph.entity.Store;

import java.util.List;

public interface StoreService {
    void add(Store store);

    void update(Store store);

    void delete(Long id);

    Store getById(Long id);

    List<Store> getList();

    void onOff(Long id);
}
