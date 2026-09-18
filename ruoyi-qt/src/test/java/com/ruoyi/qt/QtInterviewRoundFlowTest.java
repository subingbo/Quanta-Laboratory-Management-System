package com.ruoyi.qt;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DictUtils;
import com.ruoyi.common.utils.file.FileValidator;
import com.ruoyi.qt.controller.QtInterviewAdminController;
import com.ruoyi.qt.domain.QtCohort;
import com.ruoyi.qt.domain.QtInterviewApplication;
import com.ruoyi.qt.domain.QtInterviewDecisionBody;
import com.ruoyi.qt.domain.QtInterviewResult;
import com.ruoyi.qt.domain.QtInterviewRound;
import com.ruoyi.qt.domain.QtInterviewScoreBody;
import com.ruoyi.qt.mapper.QtCohortMapper;
import com.ruoyi.qt.mapper.QtInterviewMapper;
import com.ruoyi.qt.service.impl.QtInterviewAdminServiceImpl;
import com.ruoyi.qt.service.impl.QtInterviewNotifier;
import com.ruoyi.qt.util.QtInterviewStatuses;
import com.ruoyi.system.service.ISysRoleService;
import com.ruoyi.system.service.ISysUserService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class QtInterviewRoundFlowTest
{
    private static final Long APP_ID = 10L;
    private static final Long USER_ID = 88L;
    private static final Long COHORT_ID = 7L;
    private static final Long QT_MEMBER_ROLE_ID = 6L;
    private static final String FIRST = "BACKEND";
    private static final String SECOND = "PRODUCT";

    @Mock
    private QtInterviewMapper qtInterviewMapper;

    @Mock
    private QtInterviewNotifier interviewNotifier;

    @Mock
    private ISysUserService userService;

    @Mock
    private QtCohortMapper qtCohortMapper;

    @Mock
    private ISysRoleService roleService;

    @InjectMocks
    private QtInterviewAdminServiceImpl service;

    private MockedStatic<DictUtils> dictUtilsMock;
    private final Map<String, QtInterviewResult> results = new HashMap<String, QtInterviewResult>();
    private QtInterviewApplication application;

    @BeforeEach
    void setUp()
    {
        dictUtilsMock = org.mockito.Mockito.mockStatic(DictUtils.class);
        dictUtilsMock.when(() -> DictUtils.getDictCache(any())).thenReturn(null);
        dictUtilsMock.when(() -> DictUtils.getDictLabel(any(), any())).thenReturn(null);
        application = application("PROCESSING");
        when(qtInterviewMapper.selectApplicationById(APP_ID)).thenReturn(application);
        when(qtInterviewMapper.selectRoundByNo(1)).thenReturn(round(1, 101L));
        when(qtInterviewMapper.selectRoundByNo(2)).thenReturn(round(2, 102L));
        when(qtInterviewMapper.selectResultByAppRoundDept(eq(APP_ID), anyLong(), anyString()))
                .thenAnswer(invocation -> results.get(key(invocation.getArgument(1), invocation.getArgument(2))));
        when(qtInterviewMapper.insertInterviewResult(any())).thenAnswer(invocation -> {
            QtInterviewResult result = invocation.getArgument(0);
            results.put(key(result.getRoundId(), result.getDepartment()), result);
            return 1;
        });
    }

    @AfterEach
    void clear()
    {
        if (dictUtilsMock != null)
        {
            dictUtilsMock.close();
        }
        SecurityContextHolder.clearContext();
        results.clear();
    }

    @Test
    void failIsReadAndWrittenAsOut()
    {
        assertEquals("OUT", QtInterviewStatuses.normalize("FAIL"));
        assertEquals("OUT", QtInterviewStatuses.normalizeDecision("FAIL"));
        assertTrue(QtInterviewStatuses.isOut("FAIL"));
        loginAsCeo();
        put(101L, FIRST, status("PASS"));
        put(102L, FIRST, scored("PENDING", 80));
        Map<String, Object> data = service.saveDecision(APP_ID, 2L, FIRST, "FAIL", "ceo");
        assertEquals("OUT", data.get("resultStatus"));
        assertEquals("OUT", results.get(key(102L, FIRST)).getResultStatus());
    }

    @Test
    void roundOnePassOutCombinationsControlRoundTwo()
    {
        loginBackend();
        put(101L, FIRST, status("PASS"));
        put(101L, SECOND, status("OUT"));
        service.saveScore(APP_ID, FIRST, 90, "tower");
        assertEquals(new BigDecimal("90"), results.get(key(102L, FIRST)).getScore());
        login(9L, "tower_p", SECOND, Set.of("qt:interview:admin:evaluate"), "qt_member");
        ServiceException blocked = assertThrows(ServiceException.class, () -> service.saveScore(APP_ID, SECOND, 80, "tower"));
        assertTrue(blocked.getMessage().contains("\u4e00\u9762"));
    }

    @Test
    void roundOneBothOutRejectsApplication()
    {
        loginAsCeo();
        service.saveDecision(APP_ID, 1L, FIRST, "OUT", "ceo");
        service.saveDecision(APP_ID, 1L, SECOND, "OUT", "ceo");
        assertEquals("REJECTED", application.getApplyStatus());
        loginBackend();
        ServiceException blocked = assertThrows(ServiceException.class, () -> service.saveScore(APP_ID, FIRST, 70, "tower"));
        assertTrue(blocked.getMessage().contains("\u4e00\u9762"));
    }

    @Test
    void roundTwoDecisionRequiresScoreAndAdvancement()
    {
        loginAsCeo();
        put(101L, FIRST, status("PASS"));
        ServiceException noScore = assertThrows(ServiceException.class,
                () -> service.saveDecision(APP_ID, 2L, FIRST, "PASS", "ceo"));
        assertTrue(noScore.getMessage().contains("\u5206\u6570"));
        put(101L, SECOND, status("OUT"));
        put(102L, SECOND, scored("PENDING", 66));
        ServiceException notAdvanced = assertThrows(ServiceException.class,
                () -> service.saveDecision(APP_ID, 2L, SECOND, "OUT", "ceo"));
        assertTrue(notAdvanced.getMessage().contains("\u4e00\u9762"));
    }

    @Test
    void scoreLocksAfterDecision()
    {
        loginBackend();
        put(101L, FIRST, status("PASS"));
        service.saveScore(APP_ID, FIRST, 77, "tower");
        loginAsCeo();
        service.saveDecision(APP_ID, 2L, FIRST, "OUT", "ceo");
        loginBackend();
        ServiceException locked = assertThrows(ServiceException.class, () -> service.saveScore(APP_ID, FIRST, 99, "tower"));
        assertTrue(locked.getMessage().contains("\u9501\u5b9a"));
    }

    @Test
    void firstChoicePassConvertsImmediately()
    {
        loginAsCeo();
        stubConvert();
        put(101L, FIRST, status("PASS"));
        put(102L, FIRST, scored("PENDING", 91));
        service.saveDecision(APP_ID, 2L, FIRST, "PASS", "ceo");
        assertEquals("OFFERED", application.getApplyStatus());
        assertEquals(FIRST, application.getOfferedDepartment());
        verify(userService).updateUser(any());
    }

    @Test
    void secondChoicePassWaitsIfFirstStillPending()
    {
        loginAsCeo();
        put(101L, FIRST, status("PASS"));
        put(101L, SECOND, status("PASS"));
        put(102L, FIRST, scored("PENDING", 70));
        put(102L, SECOND, scored("PENDING", 95));
        service.saveDecision(APP_ID, 2L, SECOND, "PASS", "ceo");
        assertEquals("PROCESSING", application.getApplyStatus());
        assertNull(application.getOfferedDepartment());
        verify(userService, never()).updateUser(any());
    }

    @Test
    void secondChoicePassConvertsWhenFirstIsOut()
    {
        loginAsCeo();
        stubConvert();
        put(101L, FIRST, status("PASS"));
        put(101L, SECOND, status("PASS"));
        put(102L, FIRST, scored("OUT", 60));
        put(102L, SECOND, scored("PENDING", 88));
        service.saveDecision(APP_ID, 2L, SECOND, "PASS", "ceo");
        assertEquals("OFFERED", application.getApplyStatus());
        assertEquals(SECOND, application.getOfferedDepartment());
        ArgumentCaptor<SysUser> captor = ArgumentCaptor.forClass(SysUser.class);
        verify(userService).updateUser(captor.capture());
        assertEquals(SECOND, captor.getValue().getMemberDepartment());
    }

    @Test
    void dualPassKeepsFirstChoice()
    {
        loginAsCeo();
        stubConvert();
        put(101L, FIRST, status("PASS"));
        put(101L, SECOND, status("PASS"));
        put(102L, FIRST, scored("PENDING", 80));
        put(102L, SECOND, scored("PENDING", 99));
        service.saveDecision(APP_ID, 2L, FIRST, "PASS", "ceo");
        service.saveDecision(APP_ID, 2L, SECOND, "PASS", "ceo");
        assertEquals(FIRST, application.getOfferedDepartment());
        ArgumentCaptor<SysUser> captor = ArgumentCaptor.forClass(SysUser.class);
        verify(userService, times(2)).updateUser(captor.capture());
        assertEquals(FIRST, captor.getAllValues().get(1).getMemberDepartment());
    }

    @Test
    void noticeRequiresQrForPassAndIsIdempotent()
    {
        loginAsCeo();
        put(101L, FIRST, status("PASS"));
        put(102L, FIRST, scored("PASS", 90));
        when(interviewNotifier.departmentLabel(FIRST)).thenReturn("\u540e\u7aef\u90e8");
        Map<String, Object> preview = service.previewNotice(APP_ID);
        assertEquals("Quanta \u540e\u7aef\u90e8\u5f55\u7528\u901a\u77e5", preview.get("subject"));
        assertEquals(FIRST, preview.get("offeredDepartment"));
        assertTrue(String.valueOf(preview.get("content")).contains("\u540e\u7aef\u90e8"));
        assertTrue(String.valueOf(preview.get("content")).contains("\u53d1\u9001\u65f6\u5c06\u5728\u6b64\u5904\u663e\u793a\u7fa4\u4e8c\u7ef4\u7801"));
        assertFalse(String.valueOf(preview.get("content")).contains("cid:qrcode"));
        ServiceException missingQr = assertThrows(ServiceException.class, () -> service.sendNotice(APP_ID, null, "ceo"));
        assertTrue(missingQr.getMessage().contains("\u4e8c\u7ef4\u7801"));

        MultipartFile qr = mock(MultipartFile.class);
        when(qr.isEmpty()).thenReturn(false);
        when(qr.getOriginalFilename()).thenReturn("group.png");
        try
        {
            when(qr.getBytes()).thenReturn(new byte[] { 1, 2, 3 });
        }
        catch (Exception ex)
        {
            throw new IllegalStateException(ex);
        }
        when(interviewNotifier.notifyApplicant(eq(USER_ID), any(), any(), any(), eq("group.png"), any(), any()))
                .thenReturn(true);
        try (MockedStatic<FileValidator> files = org.mockito.Mockito.mockStatic(FileValidator.class))
        {
            files.when(() -> FileValidator.validate(any(), any(), anyLong())).thenAnswer(invocation -> null);
            Map<String, Object> sent = service.sendNotice(APP_ID, qr, "ceo");
            assertEquals("SENT", sent.get("noticeStatus"));
            assertTrue(String.valueOf(sent.get("content")).contains("cid:qrcode"));
        }
        application.setNoticeStatus("SENT");
        ServiceException duplicate = assertThrows(ServiceException.class, () -> service.sendNotice(APP_ID, qr, "ceo"));
        assertTrue(duplicate.getMessage().contains("\u91cd\u590d"));
    }

    @Test
    void rejectNoticeDoesNotNeedQrAndFailedSendKeepsUnsent()
    {
        loginAsCeo();
        put(101L, FIRST, status("PASS"));
        put(102L, FIRST, scored("OUT", 55));
        when(interviewNotifier.notifyApplicant(eq(USER_ID), any(), any(), any(), any(), any(), any())).thenReturn(false);
        ServiceException failed = assertThrows(ServiceException.class, () -> service.sendNotice(APP_ID, null, "ceo"));
        assertTrue(failed.getMessage().contains("\u90ae\u4ef6"));
        Map<String, Object> preview = service.previewNotice(APP_ID);
        assertEquals("\u6dd8\u6c70\u901a\u77e5", preview.get("subject"));
        assertFalse(String.valueOf(preview.get("content")).contains("<img"));
        assertFalse("SENT".equalsIgnoreCase(application.getNoticeStatus()));
        verify(qtInterviewMapper, never()).updateApplicationAdminFields(any());
    }

    @Test
    void noticeUsesSecondChoiceTemplateWhenFirstIsOut()
    {
        loginAsCeo();
        put(101L, FIRST, status("PASS"));
        put(101L, SECOND, status("PASS"));
        put(102L, FIRST, scored("OUT", 60));
        put(102L, SECOND, scored("PASS", 88));
        Map<String, Object> preview = service.previewNotice(APP_ID);
        assertEquals("Quanta \u4ea7\u54c1\u90e8\u5f55\u7528\u901a\u77e5", preview.get("subject"));
        assertEquals(SECOND, preview.get("offeredDepartment"));
        assertTrue(String.valueOf(preview.get("content")).contains("\u4ea7\u54c1\u90e8"));
        assertFalse(String.valueOf(preview.get("content")).contains("\u670d\u52a1\u7aef"));
    }

    @Test
    void noticeUsesFirstChoiceTemplateWhenBothPass()
    {
        loginAsCeo();
        put(101L, FIRST, status("PASS"));
        put(101L, SECOND, status("PASS"));
        put(102L, FIRST, scored("PASS", 80));
        put(102L, SECOND, scored("PASS", 99));
        Map<String, Object> preview = service.previewNotice(APP_ID);
        assertEquals("Quanta \u540e\u7aef\u90e8\u5f55\u7528\u901a\u77e5", preview.get("subject"));
        assertEquals(FIRST, preview.get("offeredDepartment"));
        assertTrue(String.valueOf(preview.get("content")).contains("\u670d\u52a1\u7aef"));
        assertFalse(String.valueOf(preview.get("content")).contains("\u4ea7\u54c1\u90e8\u9762\u8bd5"));
    }

    @Test
    void listRoundTwoUsesOwnDepartmentScoreOrder()
    {
        loginBackend();
        QtInterviewApplication query = new QtInterviewApplication();
        query.setRoundId(2L);
        service.selectAdminList(query);
        ArgumentCaptor<QtInterviewApplication> captor = ArgumentCaptor.forClass(QtInterviewApplication.class);
        verify(qtInterviewMapper).selectAdminApplicationList(captor.capture());
        assertEquals(FIRST, captor.getValue().getSortDepartment());
        assertEquals(FIRST, captor.getValue().getScopedDepartment());
    }

    @Test
    void listRoundTwoWithoutDepartmentSortsByMaxAdvancedScore()
    {
        loginAsCeo();
        QtInterviewApplication query = new QtInterviewApplication();
        query.setRoundId(2L);
        service.selectAdminList(query);
        ArgumentCaptor<QtInterviewApplication> captor = ArgumentCaptor.forClass(QtInterviewApplication.class);
        verify(qtInterviewMapper).selectAdminApplicationList(captor.capture());
        assertNull(captor.getValue().getSortDepartment());
        assertNull(captor.getValue().getScopedDepartment());
    }

    @Test
    void mapperExposesSecondRoundScoresAndFiltersAdvancedCandidates() throws Exception
    {
        byte[] bytes = getClass().getClassLoader().getResourceAsStream("mapper/QtInterviewMapper.xml").readAllBytes();
        String xml = new String(bytes, StandardCharsets.UTF_8);
        int start = xml.indexOf("id=\"selectAdminApplicationList\"");
        int end = xml.indexOf("</select>", start);
        String sql = xml.substring(start, end);
        assertTrue(sql.contains("first_choice_second_round_score"));
        assertTrue(sql.contains("second_choice_second_round_score"));
        assertTrue(sql.contains("first_choice_second_round_score_update_by"));
        assertTrue(sql.contains("FAIL' then 'OUT'"));
        assertTrue(sql.contains("roundId == 2"));
        assertTrue(sql.contains("r.result_status = 'PASS'"));
        assertTrue(sql.contains("sortDepartment"));
    }

    @Test
    void newAdminApisKeepOfferAndEvaluatePermissions() throws Exception
    {
        Method decision = QtInterviewAdminController.class.getMethod("decision", Long.class, Long.class, String.class,
                QtInterviewDecisionBody.class);
        Method score = QtInterviewAdminController.class.getMethod("score", Long.class, String.class,
                QtInterviewScoreBody.class);
        Method preview = QtInterviewAdminController.class.getMethod("noticePreview", Long.class);
        Method notice = QtInterviewAdminController.class.getMethod("notice", Long.class, MultipartFile.class);
        assertTrue(decision.getAnnotation(PreAuthorize.class).value().contains("qt:interview:admin:offer"));
        assertTrue(score.getAnnotation(PreAuthorize.class).value().contains("qt:interview:admin:evaluate"));
        assertTrue(preview.getAnnotation(PreAuthorize.class).value().contains("qt:interview:admin:offer"));
        assertTrue(notice.getAnnotation(PreAuthorize.class).value().contains("qt:interview:admin:offer"));
    }

    private void stubConvert()
    {
        when(userService.selectUserById(USER_ID)).thenReturn(freshman());
        SysRole role = new SysRole();
        role.setRoleId(QT_MEMBER_ROLE_ID);
        role.setRoleKey("qt_member");
        when(roleService.selectRoleAll()).thenReturn(List.of(role));
        QtCohort cohort = new QtCohort();
        cohort.setCohortId(COHORT_ID);
        when(qtCohortMapper.selectCurrentCohort()).thenReturn(cohort);
        when(qtCohortMapper.selectRecord(eq(USER_ID), eq(COHORT_ID))).thenReturn(null);
    }

    private void put(Long roundId, String department, QtInterviewResult result)
    {
        results.put(key(roundId, department), result);
    }

    private QtInterviewResult scored(String status, int score)
    {
        QtInterviewResult result = status(status);
        result.setResultId(score + 1000L);
        result.setScore(BigDecimal.valueOf(score));
        return result;
    }

    private QtInterviewResult status(String status)
    {
        QtInterviewResult result = new QtInterviewResult();
        result.setResultStatus(status);
        return result;
    }

    private String key(Long roundId, String department)
    {
        return roundId + ":" + department;
    }

    private QtInterviewApplication application(String applyStatus)
    {
        QtInterviewApplication item = new QtInterviewApplication();
        item.setApplicationId(APP_ID);
        item.setUserId(USER_ID);
        item.setRealName("\u674e\u540c\u5b66");
        item.setEmail("li@test.com");
        item.setFirstChoice(FIRST);
        item.setSecondChoice(SECOND);
        item.setApplyStatus(applyStatus);
        return item;
    }

    private QtInterviewRound round(int roundNo, long roundId)
    {
        QtInterviewRound round = new QtInterviewRound();
        round.setRoundNo(roundNo);
        round.setRoundId(roundId);
        return round;
    }

    private SysUser freshman()
    {
        SysUser user = new SysUser();
        user.setUserId(USER_ID);
        user.setUserName("freshman_88");
        user.setIsQuantaMember("0");
        return user;
    }

    private void loginAsCeo()
    {
        login(1L, "ceo", null, Set.of(), "ceo");
    }

    private void loginBackend()
    {
        login(8L, "tower_a", FIRST, Set.of("qt:interview:admin:evaluate"), "qt_member");
    }

    private void login(Long userId, String userName, String department, Set<String> permissions, String roleKey)
    {
        SysUser user = new SysUser();
        user.setUserId(userId);
        user.setUserName(userName);
        user.setIsQuantaMember("1");
        user.setMemberDepartment(department);
        SysRole role = new SysRole();
        role.setRoleKey(roleKey);
        user.setRoles(List.of(role));
        LoginUser loginUser = new LoginUser(userId, 100L, user, new HashSet<String>(permissions));
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
