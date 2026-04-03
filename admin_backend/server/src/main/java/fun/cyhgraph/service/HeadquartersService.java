package fun.cyhgraph.service;

import fun.cyhgraph.entity.Headquarters;

import java.util.List;

public interface HeadquartersService {
    void add(Headquarters headquarters);

    void update(Headquarters headquarters);

    void delete(Long id);

    Headquarters getById(Long id);

    List<Headquarters> getList();

    List<Headquarters> getEnabledOptions();

    void onOff(Long id);
}
