package com.github.zavier.table.relation.dao.mapper;

import com.github.zavier.table.relation.dao.entity.TableRelation;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TableRelationMapper {

    List<TableRelation> selectAll();

    int insert(TableRelation tableRelation);

    int update(TableRelation tableRelation);

    int deleteById(Integer id);
}