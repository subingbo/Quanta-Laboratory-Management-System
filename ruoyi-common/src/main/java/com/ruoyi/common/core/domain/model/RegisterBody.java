package com.ruoyi.common.core.domain.model;

/**
 * 用户注册对象（实验室：仅支持新生自助注册）
 *
 * @author ruoyi
 */
public class RegisterBody extends LoginBody
{
    /**
     * 昵称/真实姓名展示
     */
    private String nickName;

    /**
     * 学号
     */
    private String studentNo;

    /**
     * 班级
     */
    private String className;

    public String getNickName()
    {
        return nickName;
    }

    public void setNickName(String nickName)
    {
        this.nickName = nickName;
    }

    public String getStudentNo()
    {
        return studentNo;
    }

    public void setStudentNo(String studentNo)
    {
        this.studentNo = studentNo;
    }

    public String getClassName()
    {
        return className;
    }

    public void setClassName(String className)
    {
        this.className = className;
    }
}
