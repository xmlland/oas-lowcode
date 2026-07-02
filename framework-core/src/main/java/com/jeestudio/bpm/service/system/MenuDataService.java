package com.jeestudio.bpm.service.system;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jeestudio.bpm.cache.cacheUtils.CacheUtil;
import com.jeestudio.bpm.common.entity.gen.GenTable;
import com.jeestudio.bpm.common.entity.system.Menu;
import com.jeestudio.bpm.common.entity.system.User;
import com.jeestudio.bpm.common.view.system.SysPermission;
import com.jeestudio.bpm.mapper.base.system.MenuDao;
import com.jeestudio.bpm.service.ai.TransService;
import com.jeestudio.bpm.service.async.AsyncService;
import com.jeestudio.bpm.service.gen.GenTableService;
import com.jeestudio.bpm.utils.UserUtil;
import com.jeestudio.bpm.utils.*;
import com.jeestudio.tools.base.utils.ConvertUtil;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Description: 菜单数据服务
 */
@Service
public class MenuDataService {

    protected static final Logger logger = LoggerFactory.getLogger(TransService.class);

    @Autowired
    private MenuDao menuDao;

    //@Autowired
    //CacheUtil cacheUtil;
    private static CacheUtil cacheUtil = new CacheUtil();

    @Autowired
    private AsyncService asyncService;

    @Autowired
    private GenTableService genTableService;

    @Transactional(readOnly = false)
    public void createMenuGroup(String formNo, String parentId, String icon) {
        boolean isView = false;
        if (formNo.indexOf("@") != -1) {
            isView = true;
        }
        User user = new User("1");
        GenTable genTable = genTableService.getGenTableWithDefination(formNo);
        //url type_=1  add type_=3 view/edit/del/lowerlevel type_=4
        Menu parentMenu = menuDao.get(parentId);
        Menu menu = new Menu();
        menu.preInsert();
        menu.setParent(parentMenu);
        menu.setParentIds(parentMenu.getParentIds() + parentId + ",");
        menu.setIsShow(Global.YES);
        menu.setType("1");
        menu.setHref("/" + genTable.getModule() + "/" + genTable.getName() + "/list");
        if (StringUtil.isNotEmpty(icon)) menu.setIcon(icon);
        menu.setName(genTable.getComments());
        menu.setNameEn(genTable.getCommentsEn());
        menu.setSort(100);
        menu.setCreateBy(user);
        menu.setUpdateBy(user);
        menuDao.insert(menu);

        if (isView) {
            Menu menuView = new Menu();
            menuView.preInsert();
            menuView.setParent(menu);
            menuView.setParentIds(menu.getParentIds() + menu.getId() + ",");
            menuView.setIsShow(Global.YES);
            menuView.setType("4");
            menuView.setName("查看");
            menuView.setNameEn("View");
            menuView.setSort(10);
            menuView.setPermission("app:" + genTable.getName() + ":view");
            menuView.setSign("view");
            menuView.setCreateBy(user);
            menuView.setUpdateBy(user);
            menuDao.insert(menuView);
        } else {
            if (StringUtil.isEmpty(genTable.getProcessDefinitionCategory())) {
                Menu menuView = new Menu();
                menuView.preInsert();
                menuView.setParent(menu);
                menuView.setParentIds(menu.getParentIds() + menu.getId() + ",");
                menuView.setIsShow(Global.YES);
                menuView.setType("4");
                menuView.setName("查看");
                menuView.setNameEn("View");
                menuView.setSort(10);
                menuView.setPermission("app:" + genTable.getName() + ":view");
                menuView.setSign("view");
                menuView.setCreateBy(user);
                menuView.setUpdateBy(user);
                menuDao.insert(menuView);

                Menu menuEdit = new Menu();
                menuEdit.preInsert();
                menuEdit.setParent(menu);
                menuEdit.setParentIds(menu.getParentIds() + menu.getId() + ",");
                menuEdit.setIsShow(Global.YES);
                menuEdit.setType("4");
                menuEdit.setName("编辑");
                menuEdit.setNameEn("Edit");
                menuEdit.setSort(20);
                menuEdit.setPermission("app:" + genTable.getName() + ":edit");
                menuEdit.setSign("edit");
                menuEdit.setCreateBy(user);
                menuEdit.setUpdateBy(user);
                menuDao.insert(menuEdit);
            }

            if (false == genTable.getTableType().equals(GenTable.TABLE_TYPE_TREE)) {
                if (StringUtil.isNotEmpty(genTable.getProcessDefinitionCategory())) {
                    Menu menuDoingButton = new Menu();
                    menuDoingButton.preInsert();
                    menuDoingButton.setParent(menu);
                    menuDoingButton.setParentIds(menu.getParentIds() + menu.getId() + ",");
                    menuDoingButton.setIsShow(Global.YES);
                    menuDoingButton.setType("3");
                    menuDoingButton.setName("办理");
                    menuDoingButton.setNameEn("Handle");
                    menuDoingButton.setSort(25);
                    menuDoingButton.setPermission("app:" + genTable.getName() + ":handle");
                    menuDoingButton.setSign("handle");
                    menuDoingButton.setCreateBy(user);
                    menuDoingButton.setUpdateBy(user);
                    menuDao.insert(menuDoingButton);
                } else {
                    Menu menuDeleteButton = new Menu();
                    menuDeleteButton.preInsert();
                    menuDeleteButton.setParent(menu);
                    menuDeleteButton.setParentIds(menu.getParentIds() + menu.getId() + ",");
                    menuDeleteButton.setIsShow(Global.YES);
                    menuDeleteButton.setType("3");
                    menuDeleteButton.setName("批量删除");
                    menuDeleteButton.setNameEn("Batch Delete");
                    menuDeleteButton.setSort(30);
                    menuDeleteButton.setPermission("app:" + genTable.getName() + ":remove");
                    menuDeleteButton.setSign("removeBtn");
                    menuDeleteButton.setCreateBy(user);
                    menuDeleteButton.setUpdateBy(user);
                    menuDao.insert(menuDeleteButton);
                }
            }

            Menu menuAdd = new Menu();
            menuAdd.preInsert();
            menuAdd.setParent(menu);
            menuAdd.setParentIds(menu.getParentIds() + menu.getId() + ",");
            menuAdd.setIsShow(Global.YES);
            menuAdd.setType("3");
            menuAdd.setIcon("fa-plus");
            menuAdd.setName("添加");
            menuAdd.setNameEn("Add");
            menuAdd.setSort(10);
            menuAdd.setPermission("app:" + genTable.getName() + ":add");
            menuAdd.setSign("addBtn");
            menuAdd.setCreateBy(user);
            menuAdd.setUpdateBy(user);
            menuDao.insert(menuAdd);

            Menu menuDelete = new Menu();
            menuDelete.preInsert();
            menuDelete.setParent(menu);
            menuDelete.setParentIds(menu.getParentIds() + menu.getId() + ",");
            menuDelete.setIsShow(Global.YES);
            menuDelete.setType("4");
            menuDelete.setName("删除");
            menuDelete.setNameEn("Delete");
            menuDelete.setSort(30);
            menuDelete.setPermission("app:" + genTable.getName() + ":del");
            menuDelete.setSign("del");
            menuDelete.setCreateBy(user);
            menuDelete.setUpdateBy(user);
            menuDao.insert(menuDelete);

            if (genTable.getTableType().equals(GenTable.TABLE_TYPE_TREE)) {
                Menu menuLowerLevel = new Menu();
                menuLowerLevel.preInsert();
                menuLowerLevel.setParent(menu);
                menuLowerLevel.setParentIds(menu.getParentIds() + menu.getId() + ",");
                menuLowerLevel.setIsShow(Global.YES);
                menuLowerLevel.setType("4");
                menuLowerLevel.setName("添加下级");
                menuLowerLevel.setNameEn("Add Sub...");
                menuLowerLevel.setSort(40);
                menuLowerLevel.setPermission("app:" + genTable.getName() + ":lowerlevel");
                menuLowerLevel.setSign("lowerlevel");
                menuLowerLevel.setCreateBy(user);
                menuLowerLevel.setUpdateBy(user);
                menuDao.insert(menuLowerLevel);
            }
        }

        //Import and Export
        if (StringUtil.isNotEmpty(genTable.getIsBuildImport()) && Global.YES.equals(genTable.getIsBuildImport())) {
            Menu menuImport = new Menu();
            menuImport.preInsert();
            menuImport.setParent(menu);
            menuImport.setParentIds(menu.getParentIds() + menu.getId() + ",");
            menuImport.setIsShow(Global.YES);
            menuImport.setType("3");
            menuImport.setName("导入");
            menuImport.setNameEn("Import");
            menuImport.setSort(13);
            menuImport.setPermission("app:" + genTable.getName() + ":import");
            menuImport.setSign("importBtn");
            menuImport.setCreateBy(user);
            menuImport.setUpdateBy(user);
            menuDao.insert(menuImport);
        }
        if (StringUtil.isNotEmpty(genTable.getIsBuildExport()) && Global.YES.equals(genTable.getIsBuildExport())) {
            Menu menuExport = new Menu();
            menuExport.preInsert();
            menuExport.setParent(menu);
            menuExport.setParentIds(menu.getParentIds() + menu.getId() + ",");
            menuExport.setIsShow(Global.YES);
            menuExport.setType("3");
            menuExport.setName("导出");
            menuExport.setNameEn("Export");
            menuExport.setSort(16);
            menuExport.setPermission("app:" + genTable.getName() + ":export");
            menuExport.setSign("exportBtn");
            menuExport.setCreateBy(user);
            menuExport.setUpdateBy(user);
            menuDao.insert(menuExport);
        }
        this.refreshMenuCache();
    }

    @Deprecated
    @Transactional(readOnly = false)
    public void saveMenu(Menu menu){
        menu.setParentIds(menu.getParent().getParentIds()
                + menu.getParent().getId() + ",");
        if (StringUtil.isBlank(menu.getId())) {
            if (!"1".equals(menu.getParentId())
                    && !"0".equals(menu.getParentId())) {
                Menu parent = menu.getParent();
                menu.setSubSystemCodeList(parent.getSubSystemCodeList());
            }
            menu.setType("1");
            menu.preInsert();
            menuDao.insert(menu);
        } else {
            menu.preUpdate();
            menuDao.update(menu);
            for (Menu childMenu : menu.getChildren()) {
                if (childMenu.getId() == null) {
                    continue;
                }
                if (Menu.DEL_FLAG_NORMAL.equals(childMenu.getDelFlag())) {
                    if (StringUtil.isBlank(childMenu.getId())) {
                        childMenu.setParent(menu);
                        childMenu.preInsert();
                        childMenu.setParentIds(menu.getParentIds()
                                + menu.getId() + ",");
                        childMenu.setIsShow(Global.NO);
                        menuDao.insert(childMenu);
                    } else {
                        childMenu.setParent(menu);
                        childMenu.setParentIds(menu.getParentIds()
                                + menu.getId() + ",");
                        childMenu.preUpdate();
                        menuDao.update(childMenu);
                    }
                } else {
                    menuDao.delete(childMenu);
                }
            }
        }
        asyncService.deleteListHash(Global.MENU_CACHE, "_hashMenu_");
        asyncService.deleteListHash(Global.MENU_CACHE, "_permission_");
    }

    @Transactional(readOnly = true)
    public List<Menu> getMenuTagTree() {
        return  menuDao.getMenuTagTree().stream().filter(menu -> !ConvertUtil.getString(menu.getParentIds()).contains(",79,")).collect(Collectors.toList());
    }

    @Async
    public void refreshMenuCache() {
        Set<String> hashKeySet = cacheUtil.getHashKeys(Global.MENU_CACHE);
        for (String hashKey : hashKeySet) {
            if (hashKey.indexOf("_hashMenu_") != -1) {
                cacheUtil.deleteLikeHashCache(Global.MENU_CACHE, hashKey);
                if (hashKey.contains(",_hashMenu_,")){
                    String[] strings = hashKey.split(",");
                    UserUtil.getMenuList(new User(strings[2]),strings[0]);
                }else{
                    UserUtil.getMenuList(new User(hashKey.substring(hashKey.indexOf("_hashMenu_") + 10)),"");
                }
            } else if (hashKey.indexOf("_permission_") != -1) {
                cacheUtil.deleteLikeHashCache(Global.MENU_CACHE, hashKey);
                UserUtil.getMenuPermissionList(new User(hashKey.substring(hashKey.indexOf("_permission_") + 12)));
            }
        }
    }

    /**
     * 强刷菜单和权限缓存
     * @param userId
     */
    public void refreshUserMenuCache(String userId) {
        String hashMenuKey = "_hashMenu_," + userId;
        cacheUtil.deleteLikeHashCache(Global.MENU_CACHE, hashMenuKey);
        UserUtil.getMenuList(new User(userId), "");
        String permissionKey = "_permission_" + userId;
        cacheUtil.deleteLikeHashCache(Global.MENU_CACHE, permissionKey);
        UserUtil.getMenuPermissionList(new User(userId));
    }

    @Transactional(readOnly = true)
    public Boolean hasPermission(String userId, String permission) {
        return menuDao.hasPermission(userId, permission) > 0;
    }

    public List<SysPermission> queryByUser(String userId) {
        return menuDao.queryByUser(userId);
    }

    public List<SysPermission> queryAll() {
        return menuDao.queryAll();
    }

    /**
     * 查询流程分类下的流程节点列表，用于创建工作流节点类型的菜单
     * @param category 流程分类
     * @return 用于创建菜单的节点列表，如果一个分类下有多个流程，返回多组节点列表
     */
    @Transactional(readOnly = true)
    public JSONArray findWrokflowNodeList(String category) {
        List<LinkedHashMap> workflowList = menuDao.findWorkflowListByCategory(category);
        JSONArray jsonArray = new JSONArray();
        for(int i = 0; i < workflowList.size(); i++) {
            List<LinkedHashMap> nodeList = menuDao.findWrokflowNodeList(StringUtil.getString(workflowList.get(i).get("key_")));
            int sort = 10;
            for(LinkedHashMap map : nodeList) {
                map.put("sort", sort);
                sort += 10;
            }
            if (nodeList.size() > 0) {
                jsonArray.addAll(nodeList);
            }
            break;
        }
        return jsonArray;
    }
}
