package fun.cyhgraph.service.serviceImpl;

import fun.cyhgraph.constant.MessageConstant;
import fun.cyhgraph.entity.TableInfo;
import fun.cyhgraph.exception.BaseException;
import fun.cyhgraph.mapper.TableInfoMapper;
import fun.cyhgraph.service.TableInfoService;
import fun.cyhgraph.vo.UserTableVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TableInfoServiceImpl implements TableInfoService {

    @Autowired
    private TableInfoMapper tableInfoMapper;

    @Override
    public UserTableVO getUserTableById(Long tableId) {
        TableInfo tableInfo = tableInfoMapper.getById(tableId);
        if (tableInfo == null) {
            throw new BaseException(MessageConstant.TABLE_NOT_FOUND);
        }
        if (tableInfo.getStatus() == null || tableInfo.getStatus() != 1) {
            throw new BaseException(MessageConstant.TABLE_DISABLED);
        }
        UserTableVO userTableVO = tableInfoMapper.getUserTableById(tableId);
        if (userTableVO == null || userTableVO.getStoreId() == null) {
            throw new BaseException(MessageConstant.STORE_NOT_FOUND);
        }
        return userTableVO;
    }

    @Override
    public TableInfo getById(Long tableId) {
        return tableInfoMapper.getById(tableId);
    }
}
