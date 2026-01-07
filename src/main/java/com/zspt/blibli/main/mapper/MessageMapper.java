package com.zspt.blibli.main.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zspt.blibli.main.mapper.domin.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
  List<Message> getMessagesAll(@Param("fUserId") Long fUserId,@Param("toUserId") Long toUserId);
}
