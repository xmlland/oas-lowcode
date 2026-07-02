package com.jeestudio.bpm.utils;

import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 请求级 ThreadLocal 缓存，消除同一请求中 Flowable 引擎服务的重复查询
 * 使用 depth 引用计数支持嵌套 init/destroy（ZformBaseService → ActService）。
 */
public class ActCacheContext {

    private static final ThreadLocal<CacheHolder> CACHE = new ThreadLocal<>();

    private static class CacheHolder {
        int depth = 0;
        final Map<String, ProcessInstance> processInstanceCache = new HashMap<>();
        final Map<String, ProcessDefinition> processDefinitionCache = new HashMap<>();
        final Map<String, BpmnModel> bpmnModelCache = new HashMap<>();
        final Map<String, Task> currentTaskCache = new HashMap<>();
        final Map<String, Task> activeTaskCache = new HashMap<>();
        final Map<String, List<Task>> taskListCache = new HashMap<>();
    }

    public static void init() {
        CacheHolder holder = CACHE.get();
        if (holder == null) {
            holder = new CacheHolder();
            CACHE.set(holder);
        }
        holder.depth++;
    }

    public static void destroy() {
        CacheHolder holder = CACHE.get();
        if (holder == null) return;
        holder.depth--;
        if (holder.depth <= 0) {
            CACHE.remove();
        }
    }

    public static boolean isActive() {
        return CACHE.get() != null;
    }

    /**
     * 失效所有可变缓存（complete() 后调用）。
     * 不可变的 ProcessDefinition 和 BpmnModel 保留。
     */
    public static void invalidateMutable() {
        CacheHolder h = CACHE.get();
        if (h == null) return;
        h.processInstanceCache.clear();
        h.currentTaskCache.clear();
        h.activeTaskCache.clear();
        h.taskListCache.clear();
    }

    // ---- ProcessInstance ----
    public static ProcessInstance getProcessInstance(String procInsId) {
        CacheHolder h = CACHE.get();
        return h == null ? null : h.processInstanceCache.get(procInsId);
    }

    public static void putProcessInstance(String procInsId, ProcessInstance pi) {
        CacheHolder h = CACHE.get();
        if (h != null && pi != null) h.processInstanceCache.put(procInsId, pi);
    }

    // ---- ProcessDefinition ----
    public static ProcessDefinition getProcessDefinition(String pdId) {
        CacheHolder h = CACHE.get();
        return h == null ? null : h.processDefinitionCache.get(pdId);
    }

    public static void putProcessDefinition(String pdId, ProcessDefinition pd) {
        CacheHolder h = CACHE.get();
        if (h != null && pd != null) h.processDefinitionCache.put(pdId, pd);
    }

    // ---- BpmnModel ----
    public static BpmnModel getBpmnModel(String pdId) {
        CacheHolder h = CACHE.get();
        return h == null ? null : h.bpmnModelCache.get(pdId);
    }

    public static void putBpmnModel(String pdId, BpmnModel model) {
        CacheHolder h = CACHE.get();
        if (h != null && model != null) h.bpmnModelCache.put(pdId, model);
    }

    // ---- CurrentTask (key: procInsId#loginName) ----
    public static Task getCurrentTask(String procInsId, String loginName) {
        CacheHolder h = CACHE.get();
        return h == null ? null : h.currentTaskCache.get(procInsId + "#" + loginName);
    }

    public static void putCurrentTask(String procInsId, String loginName, Task task) {
        CacheHolder h = CACHE.get();
        if (h != null && task != null) h.currentTaskCache.put(procInsId + "#" + loginName, task);
    }

    // ---- ActiveTask (key: procInsId#taskDefKey) ----
    public static Task getActiveTask(String procInsId, String taskDefKey) {
        CacheHolder h = CACHE.get();
        return h == null ? null : h.activeTaskCache.get(procInsId + "#" + taskDefKey);
    }

    public static void putActiveTask(String procInsId, String taskDefKey, Task task) {
        CacheHolder h = CACHE.get();
        if (h != null && task != null) h.activeTaskCache.put(procInsId + "#" + taskDefKey, task);
    }

    // ---- TaskList (key: procInsId) ----
    public static List<Task> getTaskList(String procInsId) {
        CacheHolder h = CACHE.get();
        return h == null ? null : h.taskListCache.get(procInsId);
    }

    public static void putTaskList(String procInsId, List<Task> tasks) {
        CacheHolder h = CACHE.get();
        if (h != null && tasks != null) h.taskListCache.put(procInsId, tasks);
    }
}
