package fun.cyhgraph.mapper;

import com.github.pagehelper.Page;
import fun.cyhgraph.dto.member.MemberPageQueryDTO;
import fun.cyhgraph.dto.user.AdminUserPageQueryDTO;
import fun.cyhgraph.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.Map;

@Mapper
public interface UserMapper {

    @Select("select * from user where username = #{username}")
    User getByUsername(String username);

    void insert(User user);

    @Select("select * from user where id = #{id}")
    User getById(Integer id);

    void update(User user);

    Page<User> pageMemberQuery(MemberPageQueryDTO queryDTO);

    Page<User> pageAdminUser(@Param("query") AdminUserPageQueryDTO queryDTO,
                             @Param("now") LocalDateTime now);

    int updateMemberInfo(@Param("userId") Integer userId,
                         @Param("isMember") Integer isMember,
                         @Param("memberLevel") Integer memberLevel,
                         @Param("memberExpireTime") LocalDateTime memberExpireTime,
                         @Param("updateTime") LocalDateTime updateTime);

    int updateUserStatus(@Param("userId") Integer userId,
                         @Param("status") Integer status,
                         @Param("updateTime") LocalDateTime updateTime);

    Integer countByMap(Map map);
}
