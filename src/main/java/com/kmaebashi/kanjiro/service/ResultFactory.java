package com.kmaebashi.kanjiro.service;

import com.kmaebashi.jsonparser.ClassMapper;
import com.kmaebashi.nctfw.JsonResult;

public class ResultFactory {
    private ResultFactory() {}

    public static JsonResult createJsonResult(Object obj) {
        String json = ClassMapper.toJson(obj);
        return new JsonResult(json);
    }
}
