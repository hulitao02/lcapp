package com.cloud.backend.dao;

import com.cloud.backend.model.PcMenu;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface PcMenuDao {


    /**
     * @author:胡立涛
     * @description: TODO 添加产品菜单
     * @date: 2022/3/16
     * @param: [pcMenu]
     * @return: void
     */
    void saveInfo(PcMenu pcMenu);

    /**
     * @author:胡立涛
     * @description: TODO 更新产品菜单
     * @date: 2022/3/16
     * @param: [pcMenu]
     * @return: void
     */
    void updateInfo(PcMenu pcMenu);

    /**
     * @author:胡立涛
     * @description: TODO 根据id删除产品菜单信息
     * @date: 2022/3/16
     * @param: [pcMenu]
     * @return: void
     */
    void delInfo(PcMenu pcMenu);


    /**
     * @author:胡立涛
     * @description: TODO 查询产品菜单列表
     * @date: 2022/3/16
     * @param: []
     * @return: java.util.List<java.util.Map>
     */
    List<Map> getInfoList();

    /**
     * @author:胡立涛
     * @description: TODO 根据id查询产品菜单详细信息
     * @date: 2022/3/16
     * @param: [pcMenu]
     * @return: com.cloud.backend.model.PcMenu
     */
    PcMenu getInfoDetail(PcMenu pcMenu);

    /**
     *
     * @author:胡立涛
     * @description: TODO 迭代查询根节点及所有子节点的数据
     * @date: 2022/7/5
     * @param: [pcMenu]
     * @return: java.util.List<java.util.Map>
     */
    List<Map> getMenuList(PcMenu pcMenu);

    /**
     *
     * @author:胡立涛
     * @description: TODO 根据ids删除菜单信息
     * @date: 2022/7/5
     * @param: [map]
     * @return: void
     */
    void delMenus(Map map);

}
