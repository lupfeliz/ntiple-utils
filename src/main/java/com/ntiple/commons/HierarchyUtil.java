/**
 * @File        : HierarchyUtil.java
 * @Author      : 정재백
 * @Since       : 2025-01-07
 * @Description : HierarchyUtil
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.commons;

import static com.ntiple.commons.ReflectionUtil.cast;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HierarchyUtil {
  public static <T extends HierarchyEntry<?>> List<T> makeHierarchy(List<T> list) { return makeHierarchy(list, null); }
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public static <T extends HierarchyEntry<?>> List<T> makeHierarchy(List<T> list, Map<String, T> pmap) {
    List<T> ret = new ArrayList<>();
    Map<String, List<T>> submap = new LinkedHashMap<>();
    List<T> working = new ArrayList<>();
    if (pmap == null) { pmap = new LinkedHashMap<>(); }
    /** 1차 LOOP 맵생성 */
    for (T itm : list) {
      pmap.put(itm.getEntryId(), itm);
      submap.put(itm.getEntryId(), new ArrayList<>());
    }
    /** 2차 LOOP 부모찾아 배열하기 */
    for (T itm : list) {
      String parentId = itm.getParentId();
      if (pmap.containsKey(parentId)) {
        List<Object> sub = cast(submap.get(parentId), sub = null);
        sub.add(itm);
        HierarchyEntry parent = cast(pmap.get(parentId), parent = null);
        parent.setChildren(sub);
      } else {
        /** 부모노드가 없다면 루트아이템으로 인식. */
        working.add(itm);
      }
    }
    /** 정렬 */
    working.sort(null);
    for (String key : submap.keySet()) {
      submap.get(key).sort(null);
    }
    /** 하위목록까지 포함한 최종 정렬 리스트 */
    ret = setSortAttributes(working, 1, pmap, submap, ret);
    return ret;
  }

  public static <T extends HierarchyEntry<?>> List<T> setSortAttributes(List<T> clist, int depth,
    Map<String, T> map, Map<String, List<T>> submap,
    List<T> nlist) {
    List<T> ret = clist;
    for (T itm : clist) {
      itm.setDepth(depth);
      itm.setSortNo(nlist.size() + 1);
      nlist.add(itm);
      if (submap.containsKey(itm.getEntryId())) {
        setSortAttributes(submap.get(itm.getEntryId()),
          depth + 1, map, submap, nlist);
      }
    }
    return ret;
  }

  @SuppressWarnings("rawtypes")
  public static interface HierarchyEntry<T extends HierarchyEntry> extends Comparable<T> {
    String getEntryId();
    String getParentId();
    void setDepth(Integer depth);
    void setSortNo(Integer sortNo);
    List<T> getChildren();
    void setChildren(List<T> children);
  }
}
