package com.zspt.blibli.main.server.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zspt.blibli.main.mapper.CategoriesMapper;
import com.zspt.blibli.main.mapper.domin.Categories;
import com.zspt.blibli.main.server.CategoriesServer;
import org.springframework.stereotype.Service;

@Service
public class CategoriesServerImpl extends ServiceImpl<CategoriesMapper, Categories> implements CategoriesServer {

}
