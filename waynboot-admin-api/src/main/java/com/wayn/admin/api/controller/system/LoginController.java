package com.wayn.admin.api.controller.system;

import com.wayn.admin.framework.security.service.LoginService;
import com.wayn.admin.framework.security.service.PermissionService;
import com.wayn.admin.framework.security.service.TokenService;
import com.wayn.common.constant.SysConstants;
import com.wayn.common.core.domain.system.Menu;
import com.wayn.common.core.domain.system.User;
import com.wayn.common.core.model.LoginObj;
import com.wayn.common.core.model.LoginUserDetail;
import com.wayn.common.core.service.system.IMenuService;
import com.wayn.common.enums.ReturnCodeEnum;
import com.wayn.common.util.IdUtil;
import com.wayn.common.util.R;
import com.wayn.data.redis.manager.RedisCache;
import com.wf.captcha.SpecCaptcha;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 用户登录
 *
 * @author wayn
 * @since 2020-07-21
 */
@RestController
@AllArgsConstructor
public class LoginController {
    private LoginService loginService;
    private TokenService tokenService;
    private PermissionService permissionService;
    private IMenuService iMenuService;
    private RedisCache redisCache;

    @PostMapping("/login")
    public R login(@RequestBody LoginObj loginObj) {
        // 获取redis中的验证码
        String redisCode = redisCache.getCacheObject(loginObj.getKey());
        // 判断验证码
        if (loginObj.getCode() == null || !redisCode.equals(loginObj.getCode().trim().toLowerCase())) {
            return R.error(ReturnCodeEnum.USER_VERIFY_CODE_ERROR);
        }
        // 删除验证码
        redisCache.deleteObject(loginObj.getKey());
        // 生成令牌
        String token = loginService.login(loginObj.getUsername(), loginObj.getPassword());
        return R.success().add(SysConstants.TOKEN, token);
    }

    @GetMapping("/getInfo")
    public R userInfo(HttpServletRequest request) {
        R success = R.success();
        LoginUserDetail loginUser = tokenService.getLoginUser(request);
        User user = loginUser.getUser();
        Set<String> rolePermission = permissionService.getRolePermission(user);
        Set<String> menuPermission = permissionService.getMenuPermission(rolePermission);
        success.add("user", user);
        success.add("roles", rolePermission);
        success.add("permissions", menuPermission);
        return success;
    }

    @GetMapping("/getRouters")
    public R getRouters(HttpServletRequest request) {
        R success = R.success();
        LoginUserDetail loginUser = tokenService.getLoginUser(request);
        // 用户信息
        User user = loginUser.getUser();
        List<Menu> menus = iMenuService.selectMenuTreeByUserId(user.getUserId());
        return success.add("routers", iMenuService.buildMenus(menus));
    }

    @GetMapping("/captcha")
    public R captcha() {
        SpecCaptcha specCaptcha = new SpecCaptcha(100, 43, 4);
        String verCode = specCaptcha.text().toLowerCase();
        String key = IdUtil.getUid();
        // 存入redis并设置过期时间为30分钟
        redisCache.setCacheObject(key, verCode, SysConstants.CAPTCHA_EXPIRATION, TimeUnit.MINUTES);
        // 将key和base64返回给前端
        return R.success().add("key", key).add("image", specCaptcha.toBase64());
    }
}
