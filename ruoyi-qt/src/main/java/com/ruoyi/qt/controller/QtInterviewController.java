package com.ruoyi.qt.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.RateLimiter;
import com.ruoyi.common.annotation.RepeatSubmit;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.LimitType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.common.utils.file.FileValidator;
import com.ruoyi.common.utils.file.MimeTypeUtils;
import com.ruoyi.qt.domain.QtInterviewApplication;
import com.ruoyi.qt.domain.QtInterviewProfile;
import com.ruoyi.qt.domain.QtInterviewResult;
import com.ruoyi.qt.service.IQtInterviewService;
import com.ruoyi.framework.config.ServerConfig;
import com.ruoyi.framework.security.ProfileAccessSigner;

@RestController
@RequestMapping("/qt/interview")
public class QtInterviewController extends BaseController
{
    @Autowired
    private IQtInterviewService qtInterviewService;

    @Autowired
    private ServerConfig serverConfig;

    @Autowired
    private ProfileAccessSigner profileAccessSigner;

    @PostMapping("/apply")
    @RateLimiter(time = 60, count = 10, limitType = LimitType.USER, key = "rate_limit:apply:")
    @RepeatSubmit(message = "正在提交简历，请勿重复提交")
    public AjaxResult apply(QtInterviewApplication application, QtInterviewProfile profile,
            @RequestParam(value = "photoFile", required = false) MultipartFile photoFile)
    {
        if (StringUtils.isEmpty(application.getFirstChoice()) || StringUtils.isEmpty(application.getSecondChoice()))
        {
            return AjaxResult.error("请完整选择两个志愿");
        }
        if (application.getFirstChoice().equals(application.getSecondChoice()))
        {
            return AjaxResult.error("两个志愿不能相同");
        }
        com.ruoyi.qt.util.QtDictUtils.requireValue(com.ruoyi.qt.util.QtDictUtils.DEPT, application.getFirstChoice(), "firstChoice");
        com.ruoyi.qt.util.QtDictUtils.requireValue(com.ruoyi.qt.util.QtDictUtils.DEPT, application.getSecondChoice(), "secondChoice");
        application.setUserId(getUserId());
        application.setApplyStatus("SUBMITTED");
        application.setUpdateBy(getUsername());
        if (StringUtils.isEmpty(application.getCreateBy()))
        {
            application.setCreateBy(getUsername());
        }

        try
        {
            if (photoFile != null && !photoFile.isEmpty())
            {
                application.setPhotoUrl(uploadPhoto(photoFile));
            }
            else if (StringUtils.isEmpty(application.getPhotoUrl()))
            {
                return AjaxResult.error("请上传证件照");
            }
        }
        catch (Exception e)
        {
            return AjaxResult.error(e.getMessage());
        }

        profile.setUserId(getUserId());
        profile.setUpdateBy(getUsername());
        if (StringUtils.isEmpty(profile.getCreateBy()))
        {
            profile.setCreateBy(getUsername());
        }
        if (StringUtils.isEmpty(profile.getCodingExperience()))
        {
            profile.setCodingExperience("0");
        }
        return toAjax(qtInterviewService.saveMyApplication(application, profile));
    }

    @GetMapping("/my")
    public AjaxResult myApplication()
    {
        QtInterviewApplication application = qtInterviewService.selectMyApplication(getUserId());
        QtInterviewProfile profile = qtInterviewService.selectMyProfile(getUserId());
        if (application != null)
        {
            fillPhotoAccessUrl(application);
        }
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("application", application);
        data.put("profile", profile);
        return success(data);
    }

    @GetMapping("/myResults")
    public AjaxResult myResults()
    {
        List<QtInterviewResult> list = qtInterviewService.selectMyResultList(getUserId());
        return success(list);
    }

    /**
     * 录入/更新一面二面等轮次结果。管理端请优先使用 /qt/interview/admin/*。
     */
    @PreAuthorize("@ss.hasPermi('qt:interview:admin:evaluate')")
    @PostMapping("/result")
    public AjaxResult saveResult(@org.springframework.web.bind.annotation.RequestBody QtInterviewResult result)
    {
        if (result.getUserId() == null)
        {
            return AjaxResult.error("userId不能为空");
        }
        if (result.getRoundId() == null)
        {
            return AjaxResult.error("roundId不能为空");
        }
        if (StringUtils.isEmpty(result.getDepartment()))
        {
            return AjaxResult.error("department不能为空");
        }
        if (StringUtils.isEmpty(result.getResultStatus()))
        {
            return AjaxResult.error("resultStatus不能为空");
        }
        if (result.getApplicationId() == null)
        {
            QtInterviewApplication application = qtInterviewService.selectMyApplication(result.getUserId());
            if (application == null)
            {
                return AjaxResult.error("该用户尚未投递简历");
            }
            result.setApplicationId(application.getApplicationId());
        }
        if (result.getPublishedTime() == null)
        {
            result.setPublishedTime(new java.util.Date());
        }
        result.setUpdateBy(getUsername());
        if (StringUtils.isEmpty(result.getCreateBy()))
        {
            result.setCreateBy(getUsername());
        }
        return toAjax(qtInterviewService.saveInterviewResult(result));
    }

    private String uploadPhoto(MultipartFile photoFile) throws Exception
    {
        String uploadPath = RuoYiConfig.getUploadPath() + "/qt/interview-photo";
        return FileUploadUtils.upload(uploadPath, photoFile, MimeTypeUtils.IMAGE_EXTENSION, FileValidator.SIZE_IMAGE);
    }

    private void fillPhotoAccessUrl(QtInterviewApplication application)
    {
        if (StringUtils.isEmpty(application.getPhotoUrl()))
        {
            return;
        }
        String imagePath = application.getPhotoUrl();
        if (imagePath.startsWith("http://") || imagePath.startsWith("https://"))
        {
            application.setPhotoAccessUrl(profileAccessSigner.signUrl(imagePath));
            return;
        }
        application.setPhotoAccessUrl(profileAccessSigner.signUrl(serverConfig.getUrl() + imagePath));
    }
}
