package net.lianbian.mall.tiny.mbg.mapper;

import java.util.List;
import net.lianbian.mall.tiny.mbg.model.UmsRolePermissionRelation;
import net.lianbian.mall.tiny.mbg.model.UmsRolePermissionRelationExample;
import org.apache.ibatis.annotations.Param;

public interface UmsRolePermissionRelationMapper {
    int countByExample(UmsRolePermissionRelationExample example);

    int deleteByExample(UmsRolePermissionRelationExample example);

    int deleteByPrimaryKey(Long id);

    int insert(UmsRolePermissionRelation record);

    int insertSelective(UmsRolePermissionRelation record);

    List<UmsRolePermissionRelation> selectByExample(UmsRolePermissionRelationExample example);

    UmsRolePermissionRelation selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") UmsRolePermissionRelation record, @Param("example") UmsRolePermissionRelationExample example);

    int updateByExample(@Param("record") UmsRolePermissionRelation record, @Param("example") UmsRolePermissionRelationExample example);

    int updateByPrimaryKeySelective(UmsRolePermissionRelation record);

    int updateByPrimaryKey(UmsRolePermissionRelation record);
}