package com.jeestudio.bpm.common.entity.common.persistence;

import com.google.common.collect.Lists;
import com.jeestudio.bpm.utils.CookieUtil;
import com.jeestudio.bpm.utils.StringUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @Description: 分页对象
 */
public class Page<T> {

    /** 当前页码，从 1 开始。 */
    protected int pageNo = 1;
    /** 每页条数，-1 表示禁用分页。 */
    protected int pageSize = 10;
    /** 总记录数，-1 表示不查询总数。 */
    protected long count;
    /** 首页页码。 */
    protected int first;
    /** 尾页页码。 */
    protected int last;
    /** 上一页页码。 */
    protected int prev;
    /** 下一页页码。 */
    protected int next;
    /** 是否首页。 */
    private boolean firstPage;
    /** 是否尾页。 */
    private boolean lastPage;
    /** 分页条显示长度。 */
    protected int length = 8;
    /** 分页滑动窗口大小。 */
    protected int slider = 1;
    /** 当前页数据列表。 */
    private List<T> list = new ArrayList<T>();
    /** 当前页 Map 结构数据列表，动态表单等场景使用。 */
    private List<LinkedHashMap> map = Lists.newArrayList();
    /** 排序表达式。 */
    private String orderBy = "";
    /** 点击页码时调用的 JS 函数名，默认 page，用于兼容旧页面。 */
    protected String funcName = "page";
    /** 分页函数附加参数。 */
    protected String funcParam = "";
    /** 分页提示信息。 */
    private String message = "";
    /** 起始偏移量，部分接口直接使用 offset 分页时设置。 */
    private int fromIndex = -1;

    public Page() {
        this.pageSize = -1;
    }

    /** 从请求中构造分页对象。 */
    public Page(HttpServletRequest request, HttpServletResponse response) {
        this(request, response, -2);
    }

    /** 按页码、每页条数和排序构造分页对象。 */
    public Page(int pageNo, int pageSize, String orderBy) {
        this.setPageNo(pageNo);
        this.setPageSize(pageSize);
        this.setOrderBy(orderBy);
    }

    /** 从请求中构造分页对象，可指定默认每页条数。 */
    public Page(HttpServletRequest request, HttpServletResponse response, int defaultPageSize) {
        String no = request.getParameter("pageNo");
        if (StringUtil.isNumeric(no)) {
            CookieUtil.setCookie(response, "pageNo", no);
            this.setPageNo(Integer.parseInt(no));
        } else if (request.getParameter("repage") != null) {
            no = CookieUtil.getCookie(request, "pageNo");
            if (StringUtil.isNumeric(no)) {
                this.setPageNo(Integer.parseInt(no));
            }
        }
        String size = request.getParameter("pageSize");
        if (StringUtil.isNumeric(size)) {
            CookieUtil.setCookie(response, "pageSize", size);
            this.setPageSize(Integer.parseInt(size));
        } else if (request.getParameter("repage") != null) {
            no = CookieUtil.getCookie(request, "pageSize");
            if (StringUtil.isNumeric(size)) {
                this.setPageSize(Integer.parseInt(size));
            }
        } else if (defaultPageSize != -2) {
            this.pageSize = defaultPageSize;
        }
        String orderBy = request.getParameter("orderBy");
        if (StringUtil.isNotBlank(orderBy)) {
            this.setOrderBy(orderBy);
        }
        Object mobileFlag = request.getAttribute("mobileFlag");
        if (mobileFlag != null && Boolean.parseBoolean(mobileFlag.toString())) {
            this.setPageNo(Integer.parseInt(request.getAttribute("pageNo").toString()));
        }
    }

    /** 按页码和每页条数构造分页对象。 */
    public Page(int pageNo, int pageSize) {
        this(pageNo, pageSize, 0);
    }

    /** 按页码、每页条数和总数构造分页对象。 */
    public Page(int pageNo, int pageSize, long count) {
        this(pageNo, pageSize, count, new ArrayList<T>());
    }

    /** 按页码、每页条数、总数和数据列表构造分页对象。 */
    public Page(int pageNo, int pageSize, long count, List<T> list) {
        this.setCount(count);
        this.setPageNo(pageNo);
        this.pageSize = pageSize;
        this.list = list;
    }

    /** 初始化分页边界、上一页、下一页和首页尾页状态。 */
    public void initialize() {
        this.first = 1;
        this.last = (int) (count / (this.pageSize < 1 ? 20 : this.pageSize) + first - 1);
        if (this.count % this.pageSize != 0 || this.last == 0) {
            this.last++;
        }
        if (this.last < this.first) {
            this.last = this.first;
        }
        if (this.pageNo <= 1) {
            this.pageNo = this.first;
            this.firstPage = true;
        }
        if (this.pageNo >= this.last) {
            this.pageNo = this.last;
            this.lastPage = true;
        }
        if (this.pageNo < this.last - 1) {
            this.next = this.pageNo + 1;
        } else {
            this.next = this.last;
        }
        if (this.pageNo > 1) {
            this.prev = this.pageNo - 1;
        } else {
            this.prev = this.first;
        }
        if (this.pageNo < this.first) {// If the current page is smaller than the first page
            this.pageNo = this.first;
        }
        if (this.pageNo > this.last) {// If the current page is larger than the last page
            this.pageNo = this.last;
        }
    }

    /** 默认不输出分页 HTML，保留给旧页面覆盖。 */
    @Override
    public String toString() {
        return "";
    }

    protected String getSelected(int pageNo, int selectedPageNo) {
        if (pageNo == selectedPageNo) {
            return "active";
        } else {
            return "";
        }
    }

    /** 获取分页 HTML 代码，默认委托 toString。 */
    public String getHtml() {
        return toString();
    }

    /** 获取总记录数。 */
    public long getCount() {
        return count;
    }

    /** 设置总记录数。 */
    public void setCount(long count) {
        this.count = count;
        if (pageSize >= count) {
            pageNo = 1;
        }
    }

    /** 获取当前页码。 */
    public int getPageNo() {
        return pageNo;
    }

    /** 设置当前页码。 */
    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    /** 获取每页条数。 */
    public int getPageSize() {
        return pageSize;
    }

    /** 设置每页条数，小于等于 0 时回退为 10。 */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize <= 0 ? 10 : pageSize; // > 500 ? 500 : pageSize;
    }

    /** 获取首页页码。 */
    public int getFirst() {
        return first;
    }

    /** 获取尾页页码。 */
    public int getLast() {
        return last;
    }

    /** 获取总页数。 */
    public int getTotalPage() {
        return getLast();
    }

    /** 是否首页。 */
    public boolean isFirstPage() {
        return firstPage;
    }

    /** 是否尾页。 */
    public boolean isLastPage() {
        return lastPage;
    }

    /** 获取上一页页码。 */
    public int getPrev() {
        if (isFirstPage()) {
            return pageNo;
        } else {
            return pageNo - 1;
        }
    }

    /** 获取下一页页码。 */
    public int getNext() {
        if (isLastPage()) {
            return pageNo;
        } else {
            return pageNo + 1;
        }
    }

    /** 获取当前页对象列表。 */
    public List<T> getList() {
        return list;
    }

    /** 设置当前页对象列表，并重新初始化分页状态。 */
    public Page<T> setList(List<T> list) {
        this.list = list;
        initialize();
        return this;
    }

    /** 获取当前页 Map 结构数据列表。 */
    public List<LinkedHashMap> getMap() {
        return map;
    }

    /** 设置当前页 Map 结构数据列表，并重新初始化分页状态。 */
    public void setMap(List<LinkedHashMap> map) {
        this.map = map;
        initialize();
    }

    /** 获取排序表达式，并进行基础 SQL 关键字过滤。 */
    public String getOrderBy() {
        // SQL filtering, prevent injection
        String reg = "(?:')|(?:--)|(/\\*(?:.|[\\n\\r])*?\\*/)|"
                + "(\\b(select|update|and|or|delete|insert|trancate|char|into|substr|ascii|declare|exec|count|master|into|drop|execute)\\b)";
        Pattern sqlPattern = Pattern.compile(reg, Pattern.CASE_INSENSITIVE);
        if (sqlPattern.matcher(orderBy).find()) {
            return "";
        }
        return orderBy;
    }

    /** 设置排序表达式，例如 updatedate desc、name asc。 */
    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    /** 获取点击页码时调用的 JS 函数名。 */
    public String getFuncName() {
        return funcName;
    }

    /** 设置点击页码时调用的 JS 函数名，默认 page。 */
    public void setFuncName(String funcName) {
        this.funcName = funcName;
    }

    /** 获取分页函数附加参数。 */
    public String getFuncParam() {
        return funcParam;
    }

    /** 设置分页函数附加参数。 */
    public void setFuncParam(String funcParam) {
        this.funcParam = funcParam;
    }

    /** 设置分页提示信息。 */
    public void setMessage(String message) {
        this.message = message;
    }

    /** 是否禁用分页。 */
    public boolean isDisabled() {
        return this.pageSize == -1;
    }

    /** 是否不查询总数。 */
    public boolean isNotCount() {
        return this.count == -1;
    }

    /** 获取 ORM 查询起始下标。 */
    public int getFirstResult() {
        int firstResult = (getPageNo() - 1) * getPageSize();
        if (firstResult >= getCount() || firstResult < 0) {
            firstResult = 0;
        }
        return firstResult;
    }

    /** 获取 ORM 查询最大返回条数。 */
    public int getMaxResults() {
        return getPageSize();
    }

    public int getFromIndex() {
        return fromIndex;
    }

    public void setFromIndex(int fromIndex) {
        this.fromIndex = fromIndex;
    }
}
