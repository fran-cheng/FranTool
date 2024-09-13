@echo off
setlocal enabledelayedexpansion

chcp 65001

:: 获取当前日期和时间，并格式化为时间戳

for /f "tokens=2 delims==" %%I in ('"wmic os get localdatetime /value"') do set "datetime=%%I"

:: 提取各个部分
set "YYYY=%datetime:~0,4%"
set "MM=%datetime:~4,2%"
set "DD=%datetime:~6,2%"
set "HH=%datetime:~8,2%"
set "Min=%datetime:~10,2%"
set "SS=%datetime:~12,2%"

:: 格式化为时间戳 YYYYMMDD_HHMMSS
set "timestamp=%YYYY%%MM%%DD%-%HH%%Min%%SS%"

echo 当前时间: %timestamp%
:: 提示用户部门
set /p department="请输入部门: "
:: 提示用户姓名
set /p NAME="请输入姓名: "

:: 检查输入是否为空
if "%NAME%"=="" (
    echo 姓名不能为空
    goto end
)


:: 修改计算机名 需要管理员权限
rem wmic computersystem where caption='%computername%' rename %NAME%




set outPutFile=%department%-%NAME%-%timestamp%.txt

echo 文件: %outPutFile%
ipconfig /all >> %outPutFile%


REM 读取临时文件内容到变量
set "output="
for /f "delims=" %%i in (%outPutFile%) do (
    set "output=!output!%%i"
)

rem 等待接口

copy %outPutFile% \\ChengLiangMing\IpMac\%outPutFile%


curl -X POST "http://manager-gz.xinghejoy.com/api/company/get_address" ^
--form "department=%department%" ^
--form "name=%NAME%" ^
--form "info=%output%"


echo 完成





:end
endlocal
pause
