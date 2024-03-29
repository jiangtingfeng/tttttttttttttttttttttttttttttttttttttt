package com.jgw.supercodeplatform.project.zaoyangpeach.controller;


import com.jgw.supercodeplatform.project.zaoyangpeach.service.FarmInfoService;
import com.jgw.supercodeplatform.trace.common.model.RestResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Map;

@RestController
@RequestMapping("/trace/zaoyangpeach/farminfo")
public class FarmInfoController {

    @Autowired
    private FarmInfoService farmInfoService;

    @GetMapping("/page")
    public RestResult listTestingType(@RequestParam @ApiIgnore Map<String, Object> map) throws Exception {
        return new RestResult(200, "success", farmInfoService.listFarmInfo(map));
    }


}
