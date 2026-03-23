package fun.cyhgraph.service.serviceImpl;

import fun.cyhgraph.entity.Headquarters;
import fun.cyhgraph.mapper.EmployeeMapper;
import fun.cyhgraph.mapper.HeadquartersMapper;
import fun.cyhgraph.exception.BaseException;
import fun.cyhgraph.context.BaseContext;
import fun.cyhgraph.service.HeadquartersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HeadquartersServiceImpl implements HeadquartersService {

    @Autowired
    private HeadquartersMapper headquartersMapper;
    @Autowired
    private EmployeeMapper employeeMapper;

    @Override
    public void add(Headquarters headquarters) {
        ensureChairman();
        if (headquarters.getStatus() == null) {
            headquarters.setStatus(1);
        }
        headquartersMapper.add(headquarters);
    }

    @Override
    public void update(Headquarters headquarters) {
        ensureChairman();
        headquartersMapper.update(headquarters);
    }

    @Override
    public void delete(Long id) {
        ensureChairman();
        headquartersMapper.delete(id);
    }

    @Override
    public Headquarters getById(Long id) {
        ensureChairman();
        return headquartersMapper.getById(id);
    }

    @Override
    public List<Headquarters> getList() {
        ensureChairman();
        return headquartersMapper.getList();
    }

    @Override
    public void onOff(Long id) {
        ensureChairman();
        headquartersMapper.onOff(id);
    }

    private void ensureChairman() {
        Integer currentId = BaseContext.getCurrentId();
        if (currentId == null) {
            throw new BaseException("未登录");
        }
        String role = employeeMapper.getById(currentId).getRole();
        if (!isChairman(role)) {
            throw new BaseException("只有董事长可以管理总店");
        }
    }

    private boolean isChairman(String role) {
        return "2".equals(role) || "CHAIRMAN".equals(role);
    }
}
