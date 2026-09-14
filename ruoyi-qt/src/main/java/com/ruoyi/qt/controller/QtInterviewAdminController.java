package com.ruoyi.qt.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.framework.config.ServerConfig;
import com.ruoyi.framework.security.ProfileAccessSigner;
import com.ruoyi.qt.domain.QtInterviewApplication;
import com.ruoyi.qt.domain.QtInterviewEvaluation;
import com.ruoyi.qt.domain.QtInterviewOfferBody;
import com.ruoyi.qt.domain.QtInterviewProfile;
import com.ruoyi.qt.domain.QtInterviewResult;
import com.ruoyi.qt.domain.QtStatusBody;
import com.ruoyi.qt.service.IQtInterviewAdminService;

@RestController
@RequestMapping("/qt/interview/admin")
public class QtInterviewAdminController extends BaseController
{
    @Autowired
    private IQtInterviewAdminService qtInterviewAdminService;

    @Autowired
    private ServerConfig serverConfig;

    @Autowired
    private ProfileAccessSigner profileAccessSigner;

    @PreAuthorize("@ss.hasPermi('qt:interview:admin:list')")
    @GetMapping("/applications")
    public TableDataInfo applications(QtInterviewApplication query,
            @RequestParam(value = "round", required = false) Long round,
            @RequestParam(value = "departmentId", required = false) String departmentId,
            @RequestParam(value = "status", required = false) String status)
    {
        applyListAliases(query, round, departmentId, status);
        startPage();
        List<QtInterviewApplication> list = qtInterviewAdminService.selectAdminList(query);
        fillPhoto(list);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('qt:interview:admin:list')")
    @GetMapping("/applications/{id:\\d+}")
    public AjaxResult application(@PathVariable("id") Long id)
    {
        QtInterviewApplication application = qtInterviewAdminService.selectApplication(id);
        fillPhoto(java.util.Collections.singletonList(application));
        QtInterviewProfile profile = qtInterviewAdminService.selectProfile(id);
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("application", application);
        data.put("profile", profile);
        return success(data);
    }

    @PreAuthorize("@ss.hasPermi('qt:interview:admin:list')")
    @GetMapping("/applications/{id:\\d+}/results")
    public AjaxResult results(@PathVariable("id") Long id)
    {
        return success(qtInterviewAdminService.selectResults(id));
    }

    @PreAuthorize("@ss.hasPermi('qt:interview:admin:list')")
    @GetMapping("/evaluations")
    public AjaxResult evaluations(QtInterviewEvaluation query)
    {
        return success(qtInterviewAdminService.selectEvaluations(query));
    }

    @PreAuthorize("@ss.hasPermi('qt:interview:admin:evaluate')")
    @Log(title = "招新面评", businessType = BusinessType.INSERT)
    @PostMapping("/evaluations")
    public AjaxResult addEvaluation(@RequestBody QtInterviewEvaluation evaluation)
    {
        return success(qtInterviewAdminService.saveEvaluation(evaluation, getUserId(), getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('qt:interview:admin:evaluate')")
    @Log(title = "招新面评", businessType = BusinessType.UPDATE)
    @PutMapping("/evaluations/{evaluationId}")
    public AjaxResult editEvaluation(@PathVariable Long evaluationId, @RequestBody QtInterviewEvaluation evaluation)
    {
        evaluation.setEvaluationId(evaluationId);
        return success(qtInterviewAdminService.updateEvaluation(evaluation, getUserId(), getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('qt:interview:admin:offer')")
    @Log(title = "招新录用", businessType = BusinessType.UPDATE)
    @PostMapping("/offers")
    public AjaxResult offers(@RequestBody QtInterviewOfferBody body)
    {
        return success(qtInterviewAdminService.offer(body, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('qt:interview:admin:list')")
    @GetMapping("/statistics")
    public AjaxResult statistics()
    {
        return success(qtInterviewAdminService.statistics());
    }

    @PreAuthorize("@ss.hasPermi('qt:interview:admin:export')")
    @Log(title = "招新导出", businessType = BusinessType.EXPORT)
    @GetMapping("/applications/export")
    public void exportGet(HttpServletResponse response, QtInterviewApplication query,
            @RequestParam(value = "round", required = false) Long round,
            @RequestParam(value = "departmentId", required = false) String departmentId,
            @RequestParam(value = "status", required = false) String status)
    {
        export(response, query, round, departmentId, status);
    }

    @PreAuthorize("@ss.hasPermi('qt:interview:admin:export')")
    @Log(title = "招新导出", businessType = BusinessType.EXPORT)
    @PostMapping("/applications/export")
    public void exportPost(HttpServletResponse response, QtInterviewApplication query)
    {
        export(response, query, query.getRoundId(), query.getDepartment(), query.getApplyStatus());
    }

    @PreAuthorize("@ss.hasPermi('qt:interview:admin:offer')")
    @Log(title = "入职状态", businessType = BusinessType.UPDATE)
    @PutMapping("/applications/{id:\\d+}/join-status")
    public AjaxResult joinStatus(@PathVariable("id") Long id, @RequestBody QtStatusBody body)
    {
        return success(qtInterviewAdminService.updateJoinStatus(id, body, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('qt:interview:admin:offer')")
    @Log(title = "最终状态", businessType = BusinessType.UPDATE)
    @PutMapping("/applications/{id:\\d+}/final-status")
    public AjaxResult finalStatus(@PathVariable("id") Long id, @RequestBody QtStatusBody body)
    {
        return success(qtInterviewAdminService.updateFinalStatus(id, body, getUsername()));
    }

    private void export(HttpServletResponse response, QtInterviewApplication query, Long round, String departmentId,
            String status)
    {
        applyListAliases(query, round, departmentId, status);
        List<QtInterviewApplication> list = qtInterviewAdminService.selectAdminList(query);
        ExcelUtil<QtInterviewApplication> util = new ExcelUtil<QtInterviewApplication>(QtInterviewApplication.class);
        util.exportExcel(response, list, "招新申请");
    }

    private void applyListAliases(QtInterviewApplication query, Long round, String departmentId, String status)
    {
        if (query.getRoundId() == null && round != null)
        {
            query.setRoundId(round);
        }
        if (StringUtils.isEmpty(query.getDepartment()) && StringUtils.isNotEmpty(departmentId))
        {
            query.setDepartment(departmentId);
        }
        if (StringUtils.isNotEmpty(status))
        {
            if (query.getRoundId() != null)
            {
                query.setResultStatus(status);
            }
            else if (StringUtils.isEmpty(query.getApplyStatus()))
            {
                query.setApplyStatus(status);
            }
        }
    }

    private void fillPhoto(List<QtInterviewApplication> list)
    {
        if (list == null)
        {
            return;
        }
        for (QtInterviewApplication application : list)
        {
            if (application == null || StringUtils.isEmpty(application.getPhotoUrl()))
            {
                continue;
            }
            String path = application.getPhotoUrl();
            if (path.startsWith("http://") || path.startsWith("https://"))
            {
                application.setPhotoAccessUrl(profileAccessSigner.signUrl(path));
            }
            else
            {
                application.setPhotoAccessUrl(profileAccessSigner.signUrl(serverConfig.getUrl() + path));
            }
        }
    }
}
