package fun.cyhgraph.service.serviceImpl;

import fun.cyhgraph.constant.MessageConstant;
import fun.cyhgraph.dto.UserDTO;
import fun.cyhgraph.dto.UserLoginDTO;
import fun.cyhgraph.dto.UserRegisterDTO;
import fun.cyhgraph.entity.User;
import fun.cyhgraph.exception.BaseException;
import fun.cyhgraph.exception.LoginFailedException;
import fun.cyhgraph.mapper.UserMapper;
import fun.cyhgraph.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 用户注册
     */
    @Override
    public void register(UserRegisterDTO userRegisterDTO) {
        String username = userRegisterDTO.getUsername();
        String password = userRegisterDTO.getPassword();
        String confirmPassword = userRegisterDTO.getConfirmPassword();
        if (!StringUtils.hasText(username)) {
            throw new BaseException(MessageConstant.USERNAME_IS_NULL);
        }
        if (!StringUtils.hasText(password)) {
            throw new BaseException(MessageConstant.PASSWORD_IS_NULL);
        }
        if (!password.equals(confirmPassword)) {
            throw new BaseException(MessageConstant.CONFIRM_PASSWORD_ERROR);
        }
        User userDB = userMapper.getByUsername(username);
        if (userDB != null) {
            throw new BaseException(MessageConstant.USERNAME_ALREADY_EXIST);
        }
        String encryptedPassword = DigestUtils.md5DigestAsHex(password.getBytes());
        String localOpenid = "LOCAL_" + DigestUtils.md5DigestAsHex(username.getBytes());
        LocalDateTime now = LocalDateTime.now();
        User user = User.builder()
                .username(username)
                .password(encryptedPassword)
                .name(userRegisterDTO.getNickname())
                .openid(localOpenid)
                .status(1)
                .createTime(now)
                .updateTime(now)
                .build();
        userMapper.insert(user);
    }

    /**
     * 用户账号密码登录
     */
    @Override
    public User login(UserLoginDTO userLoginDTO) {
        String username = userLoginDTO.getUsername();
        String password = userLoginDTO.getPassword();
        if (!StringUtils.hasText(username)) {
            throw new BaseException(MessageConstant.USERNAME_IS_NULL);
        }
        if (!StringUtils.hasText(password)) {
            throw new BaseException(MessageConstant.PASSWORD_IS_NULL);
        }
        User user = userMapper.getByUsername(username);
        if (user == null) {
            throw new LoginFailedException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        String encryptedPassword = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!encryptedPassword.equals(user.getPassword())) {
            throw new LoginFailedException(MessageConstant.PASSWORD_ERROR);
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BaseException(MessageConstant.USER_ACCOUNT_LOCKED);
        }
        return user;
    }

    /**
     * 根据id查询用户
     * @param id
     * @return
     */
    public User getUser(Integer id) {
        return userMapper.getById(id);
    }

    /**
     * 修改用户信息
     * @param userDTO
     */
    public void update(UserDTO userDTO) {
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.update(user);
    }
}
