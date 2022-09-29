package com.fran.util;

import com.fran.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 程良明
 * @date 2022/9/29
 * * 说明:合并smaliFile,资源等
 * 合并2个apk
 **/
public class MergeFiles {

    /**
     * 目标路径
     */
    private String targetFolder;
    /**
     * 资源路径
     */
    private String sourceFolder;

    public void execute() {
        FileUtils source = new FileUtils(sourceFolder, "", false, true);
        FileUtils target = new FileUtils(targetFolder, "", false, true);
    }


    /**
     * 替换文件
     */
    private void replaceFile() {
        //        assets
//        smali
//        lib
//        res 。 res/value 下的是合并
//        需要copy的所有文件
        List<File> needCopyFiles = new ArrayList<>();

        // TODO: 2022/9/29 收集所有需要copy的文件，并确认对应的路径  ，需要注意smali可能有多个 需要区分拷贝到那个具体的smali下，默认是最后一个
    }

    /**
     * 需要合并的文件
     */
    private void mergeFile() {
//        res/value
//        androidMainXml
    }
}
