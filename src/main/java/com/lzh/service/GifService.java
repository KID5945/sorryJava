package com.lzh.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import com.google.common.base.Splitter;
import com.lzh.entity.Subtitles;

import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.Getter;
import lombok.Setter;

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
            
            handleProcess(exec);
            //logger.info("输出正常:{}",IOUtils.toString(exec.getInputStream()));
            //logger.info("输出错误:{}",IOUtils.toString(exec.getErrorStream()));
            
            exec.waitFor();
            
        } catch (Exception e) {
            logger.error("生成gif报错：{}", e);
        }
        return tempPath+gifRelativePath;
    }

    private void handleProcess(Process process) {
        new Thread() {
            @Override
            public void run() {
                BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = null;

                try {
                    while ((line = in.readLine()) != null) {
                        logger.info("ffmpeg执行的结果为: "+line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();

        new Thread(){
            @Override
            public void run()
            {
                BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String line = null;
                StringBuilder result=new StringBuilder();
                try
                {
                    while((line = err.readLine()) != null)
                    {
                        result.append(line);
                    }
                    logger.info("ffmpeg执行的错误: "+result);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    try
                    {
                        err.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
         
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
        BufferedWriter writer = null;
        try{
            writer = 
                new BufferedWriter (
                    new OutputStreamWriter (
                        new FileOutputStream (path.toFile(),true),"UTF-8"));
            temp.process(root, writer);
        } catch (Exception e) {
            logger.error("生成ass文件报错", e);
        }finally {
            if(writer != null) {
                try {
                    writer.close();
                } catch (Exception e) {   
                     e.printStackTrace();
                }
            }
        }
        return path.toString();
    }


}
