把aab 中 Bundleconfig.pb 放到一起，cmd 打开，切换到解压目录，执行下面命令即可

protoc --decode=android.bundle.BundleConfig config.proto < Bundleconfig.pb > bundleconfig.txt


参考 
https://stackoverflow.com/questions/73759646/how-to-decode-android-app-bundle-pb-files