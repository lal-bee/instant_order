package fun.cyhgraph.service;

import fun.cyhgraph.entity.TableInfo;
import fun.cyhgraph.vo.UserTableVO;

public interface TableInfoService {

    UserTableVO getUserTableById(Long tableId);

    TableInfo getById(Long tableId);
}
