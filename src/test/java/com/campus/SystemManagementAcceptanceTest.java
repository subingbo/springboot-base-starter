package com.campus;

import com.campus.auth.dto.TokenResponse;
import com.campus.system.dict.dto.DictDataRequest;
import com.campus.system.dict.dto.DictTypeRequest;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SystemManagementAcceptanceTest extends AbstractIntegrationTest {

    @Test
    void dataScopeLimitsUsersToOwnDept() throws Exception {
        TokenResponse admin = adminLogin();
        String suffix = String.valueOf(System.nanoTime());
        Long deptA = createDept(bearer(admin), "范围A_" + suffix);
        Long deptB = createDept(bearer(admin), "范围B_" + suffix);
        Long roleId = createRole(bearer(admin), "DEPT_" + suffix, 3);
        assignMenus(bearer(admin), roleId, List.of(100L, 1001L));

        String userA = "u1_" + suffix;
        String userB = "u2_" + suffix;
        String tester = "tester_" + suffix;
        createUser(bearer(admin), userA, deptA, List.of());
        createUser(bearer(admin), userB, deptB, List.of());
        createUser(bearer(admin), tester, deptA, List.of(roleId));

        TokenResponse testerTokens = login(tester, "Password1");
        JsonNode page = getJson("/api/system/users?pageNum=1&pageSize=100", bearer(testerTokens));
        Set<String> usernames = new HashSet<>();
        page.path("data").path("list").forEach(node -> usernames.add(node.path("username").asText()));

        assertThat(usernames).contains(tester, userA).doesNotContain(userB);
    }

    @Test
    void dictCacheIsEvictedOnUpdate() throws Exception {
        TokenResponse admin = adminLogin();
        String suffix = String.valueOf(System.nanoTime());
        String dictType = "test_dict_" + suffix;

        DictTypeRequest typeRequest = new DictTypeRequest();
        typeRequest.setDictName("测试字典");
        typeRequest.setDictType(dictType);
        typeRequest.setStatus(1);
        postJson("/api/system/dict-types", bearer(admin), typeRequest);

        DictDataRequest dataRequest = new DictDataRequest();
        dataRequest.setDictType(dictType);
        dataRequest.setDictLabel("标签A");
        dataRequest.setDictValue("1");
        dataRequest.setDictSort(1);
        dataRequest.setStatus(1);
        Long dataId = postJson("/api/system/dict-data", bearer(admin), dataRequest)
                .path("data").asLong();

        JsonNode first = getJson("/api/system/dict-data/option/" + dictType, bearer(admin));
        assertThat(first.path("data").get(0).path("dictLabel").asText()).isEqualTo("标签A");

        dataRequest.setDictLabel("标签B");
        putJson("/api/system/dict-data/" + dataId, bearer(admin), dataRequest);

        JsonNode second = getJson("/api/system/dict-data/option/" + dictType, bearer(admin));
        assertThat(second.path("data").get(0).path("dictLabel").asText()).isEqualTo("标签B");
    }

    @Test
    void operationLogIsRecordedAsynchronously() throws Exception {
        TokenResponse admin = adminLogin();
        String deptName = unique("操作日志部门");
        createDept(bearer(admin), deptName);
        await(() -> hasTotal(
                "/api/system/operation-logs?pageNum=1&pageSize=10&operName=admin",
                bearer(admin)), "operation log was not recorded");
    }

    @Test
    void adminRoutesAreAvailable() throws Exception {
        TokenResponse admin = adminLogin();
        JsonNode routes = getJson("/api/system/menus/routes", bearer(admin));
        assertThat(routes.path("data").isArray()).isTrue();
        assertThat(routes.path("data").size()).isGreaterThan(0);
    }
}
