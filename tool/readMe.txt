Mac处理
删除扩展属性
xattr -d -r com.apple.quarantine 存放路径
添加权限。
chmod -R +x 存放路径
添加环境变量 vim ~./bash_profile
# JDK 8
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_321.jdk/Contents/Home
export PATH=$PATH:$JAVA_HOME/bin
#FranTool
export TOOL_HOME=/Library/Tool
export PATH=$PATH:$TOOL_HOME

source ~/.bash_profile 

Window
1、将路径添加到环境路径 path
2、选择需要的.reg文件添加注册表
