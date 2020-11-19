package com.lzh.service;

import com.google.common.base.Splitter;
import com.lzh.entity.Subtitles;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by lizhihao on 2018/3/11.
 */
@Service
@Getter
@Setter
@ConfigurationProperties(prefix = "cache.template")
public class GifService {

    private static final Logger logger = LoggerFactory.getLogger(GifService.class);

    private String tempPath;

    public String renderGif(Subtitles subtitles) throws Exception {
        File dir = new File(tempPath);
        
        String assRelativePath = UUID.randomUUID().toString().replace("-", "") + ".ass";
        
        renderAss(subtitles,assRelativePath);
        
        String gifRelativePath = UUID.randomUUID() + ".gif";
        
        String videoRelativePath = subtitles.getTemplateName()+File.separator+"template.mp4";
        
        String cmd = String.format("ffmpeg -i %s -r 6 -vf ass=%s,scale=300:-1 -y %s", videoRelativePath, assRelativePath, gifRelativePath);
        if ("simple".equals(subtitles.getMode())) {
//            cmd = String.format("ffmpeg -i %s -r 2 -vf ass=%s,scale=250:-1 -f gif - |gifsicle --optimize=3 --delay=20 > %s ", videoPath, assPath, gifPath);
            cmd = String.format("ffmpeg -i %s -r 5 -vf ass=%s,scale=180:-1 -y %s ", videoRelativePath, assRelativePath, gifRelativePath);
        }
        logger.info("cmd: {}", cmd);
        try {
            Process exec = Runtime.getRuntime().exec(cmd,null,dir);
            exec.waitFor();
//            logger.info("输出:{}",IOUtils.toString(exec.getErrorStream()));
        } catch (Exception e) {
            logger.error("生成gif报错：{}", e);
        }
        return tempPath+gifRelativePath;
    }

    private String renderAss(Subtitles subtitles,String assRelativePath) throws Exception {
        Path path = Paths.get(tempPath).resolve(assRelativePath);
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_27);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setDirectoryForTemplateLoading(Paths.get(tempPath).resolve(subtitles.getTemplateName()).toFile());
        Map<String, Object> root = new HashMap<>();
        Map<String, String> mx = new HashMap<>();
        List<String> list = Splitter.on(",").splitToList(subtitles.getSentence());
        for (int i = 0; i < list.size(); i++) {
            mx.put("sentences" + i, list.get(i));
        }
        root.put("mx", mx);
        Template temp = cfg.getTemplate("template.ftl");
        try (FileWriter writer = new FileWriter(path.toFile())) {
            temp.process(root, writer);
        } catch (Exception e) {
            logger.error("生成ass文件报错", e);
        }
        return path.toString();
    }


}
