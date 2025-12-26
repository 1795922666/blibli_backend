package com.zspt.blibli.dynamic.server;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zspt.blibli.dynamic.mapper.domin.Dynamic;
import com.zspt.blibli.dynamic.mapper.domin.DynamicPicture;
import com.zspt.blibli.common.vo.Result;

import java.util.Date;
import java.util.List;


public interface DynamicPictureServer extends IService<DynamicPicture > {

    Result addPicture(DynamicPicture dynamicPicture);


    List<DynamicPicture> queryByNonPrimaryIds(List<Long> userIdList);



}
