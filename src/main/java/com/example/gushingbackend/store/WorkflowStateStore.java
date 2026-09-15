package com.example.gushingbackend.store;

import com.example.gushingbackend.model.bo.WorkflowTextToVideoBO;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * 工作流状态存储（内存实现）。
 * <p>
 * 以 workflowId 为键保存工作流 BO 的实时状态，供前端轮询查询。
 * 当前为内存实现，重启后状态丢失；后续可替换为 Redis 等持久化存储。
 */
@Component
public class WorkflowStateStore {

    private final ConcurrentHashMap<String, WorkflowTextToVideoBO> store = new ConcurrentHashMap<>();

    /** 保存或更新工作流状态 */
    public void save(WorkflowTextToVideoBO bo) {
        if (bo != null && bo.getWorkflowId() != null) {
            store.put(bo.getWorkflowId(), bo);
        }
    }

    /** 按 workflowId 查询工作流状态 */
    public WorkflowTextToVideoBO get(String workflowId) {
        return store.get(workflowId);
    }

    /** 判断工作流是否存在 */
    public boolean exists(String workflowId) {
        return store.containsKey(workflowId);
    }

    /** 删除工作流状态（可选清理） */
    public void remove(String workflowId) {
        store.remove(workflowId);
    }
}
