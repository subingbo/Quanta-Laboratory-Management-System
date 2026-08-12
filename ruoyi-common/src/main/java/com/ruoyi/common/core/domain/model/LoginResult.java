package com.ruoyi.common.core.domain.model;

/**
 * 登录结果
 *
 * @author ruoyi
 */
public class LoginResult
{
    /**
     * 令牌
     */
    private String token;

    /**
     * 是否塔员(0新生 1塔员)
     */
    private String isQuantaMember;

    public LoginResult()
    {
    }

    public LoginResult(String token, String isQuantaMember)
    {
        this.token = token;
        this.isQuantaMember = isQuantaMember;
    }

    public String getToken()
    {
        return token;
    }

    public void setToken(String token)
    {
        this.token = token;
    }

    public String getIsQuantaMember()
    {
        return isQuantaMember;
    }

    public void setIsQuantaMember(String isQuantaMember)
    {
        this.isQuantaMember = isQuantaMember;
    }
}
