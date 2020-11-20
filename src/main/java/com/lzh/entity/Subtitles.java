package com.lzh.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by lizhihao on 2018/3/11.
 */
@Getter
@Setter
public class Subtitles {

    private String templateName;

    private String sentence;

    private String mode;
    
    @Override
    public String toString() {
        return "Subtitles [templateName=" + templateName + ", sentence=" + sentence + ", mode=" + mode + "]";
    }
    
}
